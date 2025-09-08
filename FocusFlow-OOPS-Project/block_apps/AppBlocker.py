import psutil
import subprocess
import time
import os
import threading
from pathlib import Path

# Read blocklist file next to this script to avoid absolute-path issues
BLOCKLIST_FILE = Path(__file__).with_name("blocklist.txt")

class AppBlocker:
    def __init__(self):
        # use a set for fast lookups and to avoid duplicates
        self.blocked_apps = set()
        self.is_blocking = False
        self.block_thread = None

    # NEW: read and normalize the blocklist file
    def _read_blocklist(self):
        try:
            if not BLOCKLIST_FILE.exists():
                BLOCKLIST_FILE.write_text(
                    "# Put one process name per line, e.g. chrome.exe\n",
                    encoding="utf-8"
                )
                return set()
            items = set()
            for line in BLOCKLIST_FILE.read_text(encoding="utf-8").splitlines():
                s = line.split("#", 1)[0].strip().lower()
                if s:
                    items.add(s)
            return items
        except Exception:
            return set()

    # NEW: sync in-memory list from file (called every loop)
    def sync_from_file(self):
        items = self._read_blocklist()
        if items != self.blocked_apps:
            self.blocked_apps = items
            print(f"[AppBlocker] Loaded {len(items)} apps from {BLOCKLIST_FILE.name}")

    def add_to_blacklist(self, app_names):
        """Add apps to blacklist - can be process names or executable names"""
        if isinstance(app_names, str):
            app_names = [app_names]
        self.blocked_apps |= {a.lower() for a in app_names}

    def remove_from_blacklist(self, app_name):
        """Remove app from blacklist"""
        self.blocked_apps.discard(app_name.lower())

    def stop_blocking(self):
        """Stop the blocking loop and join the worker thread."""
        self.is_blocking = False
        if self.block_thread and self.block_thread.is_alive():
            try:
                self.block_thread.join(timeout=2)
            except Exception:
                pass

    # METHOD 1: Process Termination (Most Common)
    def kill_blocked_processes(self):
        """Continuously kill blocked processes"""
        killed_apps = []
        EXEMPT = {
            "system","idle","services.exe","smss.exe","lsass.exe","csrss.exe",
            "wininit.exe","winlogon.exe","svchost.exe","fontdrvhost.exe","registry",
            "explorer.exe","dwm.exe","python.exe","pythonw.exe"
        }
        for proc in psutil.process_iter(['pid', 'name', 'exe']):
            try:
                process_name = (proc.info['name'] or "").lower()
                exe_path = (proc.info['exe'] or "").lower()

                if not process_name or process_name in EXEMPT:
                    continue

                if (process_name in self.blocked_apps) or any(b in exe_path for b in self.blocked_apps):
                    proc.terminate()
                    killed_apps.append(process_name)
                    print(f"Blocked: {process_name} (pid={proc.info['pid']})")
                    try:
                        proc.wait(timeout=2)
                    except psutil.TimeoutExpired:
                        proc.kill()

            except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
                pass
        return killed_apps

    # METHOD 2: File Renaming (Windows/Linux)
    def rename_executables(self, block=True):
        """Temporarily rename executable files to block them"""
        common_paths = [
            "C:\\Program Files\\",
            "C:\\Program Files (x86)\\",
            os.path.expanduser("~/Applications/"),  # macOS
            "/usr/bin/",  # Linux
            "/opt/"  # Linux
        ]
        
        for blocked_app in self.blocked_apps:
            for base_path in common_paths:
                if os.path.exists(base_path):
                    for root, dirs, files in os.walk(base_path):
                        for file in files:
                            if blocked_app.lower() in file.lower() and file.endswith('.exe'):
                                original_path = os.path.join(root, file)
                                if block:
                                    blocked_path = original_path + '.blocked'
                                    try:
                                        os.rename(original_path, blocked_path)
                                        print(f"Blocked executable: {file}")
                                    except PermissionError:
                                        print(f"Permission denied for: {file}")
                                else:
                                    # Unblock by renaming back
                                    if original_path.endswith('.blocked'):
                                        try:
                                            os.rename(original_path, original_path[:-8])
                                            print(f"Unblocked executable: {file}")
                                        except PermissionError:
                                            pass

    # METHOD 3: Registry/Startup Blocking (Windows)
    def block_via_registry(self, block=True):
        """Block apps from starting via registry (Windows only)"""
        try:
            import winreg
            
            # Access the Image File Execution Options registry key
            reg_path = r"SOFTWARE\Microsoft\Windows NT\CurrentVersion\Image File Execution Options"
            
            for blocked_app in self.blocked_apps:
                app_key = f"{reg_path}\\{blocked_app}"
                
                if block:
                    try:
                        key = winreg.CreateKey(winreg.HKEY_LOCAL_MACHINE, app_key)
                        winreg.SetValueEx(key, "Debugger", 0, winreg.REG_SZ, "nonexistent.exe")
                        winreg.CloseKey(key)
                        print(f"Registry blocked: {blocked_app}")
                    except PermissionError:
                        print("Need admin privileges for registry modification")
                else:
                    try:
                        winreg.DeleteKey(winreg.HKEY_LOCAL_MACHINE, app_key)
                        print(f"Registry unblocked: {blocked_app}")
                    except (FileNotFoundError, PermissionError):
                        pass
                        
        except ImportError:
            print("Registry method only works on Windows")

    # METHOD 4: Continuous Monitoring and Blocking
    def start_blocking(self, duration_minutes=None):
        """Start the blocking process in a separate thread"""
        self.is_blocking = True

        def blocking_loop():
            start_time = time.time()
            while self.is_blocking:
                # reload from file every cycle
                self.sync_from_file()
                self.kill_blocked_processes()
                time.sleep(2)  # Check every 2 seconds
                if duration_minutes and (time.time() - start_time) > (duration_minutes * 60):
                    self.stop_blocking()

        self.block_thread = threading.Thread(target=blocking_loop, daemon=True)
        self.block_thread.start()
    print(f"Started blocking (reading from {BLOCKLIST_FILE})")

# Example Usage
if __name__ == "__main__":
    blocker = AppBlocker()
    blocker.start_blocking(duration_minutes=None)
    try:
        while blocker.is_blocking:
            time.sleep(1)
    except KeyboardInterrupt:
        blocker.stop_blocking()
        print("\nBlocking stopped by user")