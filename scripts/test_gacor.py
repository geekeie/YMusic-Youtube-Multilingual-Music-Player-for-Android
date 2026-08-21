import subprocess
import time
import sys

def run_adb(command):
    result = subprocess.run(f"adb {command}", shell=True, capture_output=True, text=True)
    return result.stdout.strip()

def test_ymusic_gacor():
    print("🚀 Initializing Operation Total Gacor Test...")
    
    # 1. Check for device
    devices = run_adb("devices")
    if "device" not in devices.split("\n")[1:]:
        print("❌ ERROR: No Android device detected! Plug in your HP, Bos!")
        return

    print("✅ Device detected. Commencing assault.")

    # 2. Launch App
    package = "com.peecock.ymusic"
    print(f"📡 Launching {package}...")
    run_adb(f"shell monkey -p {package} -c android.intent.category.LAUNCHER 1")
    time.sleep(5)

    # 3. Search Verification
    print("🔍 Testing Search Functionality (Searching for Rick Astley)...")
    # Coordinates might vary, but we use key events for search
    run_adb("shell input keyevent 84") # Search button
    time.sleep(2)
    run_adb("shell input text 'Rick%sAstley'")
    run_adb("shell input keyevent 66") # Enter
    time.sleep(5)

    # 4. Playback Check (Looking for Media Session)
    print("🎵 Checking Playback Status...")
    media_status = run_adb("shell dumpsys media_session")
    if package in media_status:
        print("🔥 SUCCESS: YMusic is playing! Content is GACOR!")
    else:
        print("⚠️ WARNING: No playback detected. Could be BotGuard or UI issue.")

    print("🏁 Operation Test Complete.")

if __name__ == "__main__":
    test_ymusic_gacor()
