import psutil
import time
import os
from pathlib import Path

SCRIPT_DIR = Path(__file__).parent
BLACKLIST_FILE = SCRIPT_DIR / "blocklist.txt"
EXEMPT = {
    "system", "idle", "services.exe", "smss.exe", "lsass.exe", "csrss.exe",
    "wininit.exe", "winlogon.exe", "svchost.exe", "fontdrvhost.exe", "registry",
    "explorer.exe", "dwm.exe", "python.exe", "pythonw.exe"
}
def load_blacklist():
    if not BLACKLIST_FILE.exists():
        # create an empty file so users can edit it easily
        try:
            BLACKLIST_FILE.write_text(
                "# Put one process name per line, e.g. chrome.exe or notepad.exe\n",
                encoding="utf-8",
            )
        except Exception:
            pass
        return set()
    return {
        line.split("#", 1)[0].strip().lower()
        for line in BLACKLIST_FILE.read_text(encoding="utf-8").splitlines()
        if line.strip()
    }

def block_apps():
    last_count = -1
    while True:
        blacklist = load_blacklist()
        if len(blacklist) != last_count:
            print(f"Loaded {len(blacklist)} app patterns from '{BLACKLIST_FILE.name}'")
            last_count = len(blacklist)
        for proc in psutil.process_iter(['pid', 'name']):
            try:
                name = (proc.info['name'] or "").lower()
                if not name or name in EXEMPT:
                    continue
                if name in blacklist:
                    print(f"Blocking app: {name} (pid={proc.info['pid']})")
                    proc.terminate()
                    try:
                        proc.wait(timeout=2)
                    except psutil.TimeoutExpired:
                        proc.kill()
            except (psutil.NoSuchProcess, psutil.AccessDenied):
                continue
        time.sleep(2)

if __name__ == "__main__":
    print("App blocker started. Press Ctrl+C to stop.")
    print(f"Reading app blocklist from: {BLACKLIST_FILE}")
    block_apps()