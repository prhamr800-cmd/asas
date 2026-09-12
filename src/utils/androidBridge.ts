// Android Native Bridge integration for Privo

type BackHandler = () => boolean; // return true if handled, false to pass through

class AndroidBridgeManager {
  private backHandlers: BackHandler[] = [];

  constructor() {
    if (typeof window !== "undefined") {
      // Expose back button hook to native Android
      (window as any).__privoHandleAndroidBack = () => {
        return this.handleBackPressed();
      };

      // Expose deep link hook to native Android
      (window as any).__privoHandleDeepLink = (url: string) => {
        console.log("[Privo Android] Received deep link:", url);
        window.dispatchEvent(new CustomEvent("privo-deep-link", { detail: { url } }));
      };
    }
  }

  public registerBackHandler(handler: BackHandler): () => void {
    this.backHandlers.push(handler);
    return () => {
      this.backHandlers = this.backHandlers.filter(h => h !== handler);
    };
  }

  public handleBackPressed(): boolean {
    for (let i = this.backHandlers.length - 1; i >= 0; i--) {
      const handled = this.backHandlers[i]();
      if (handled) return true;
    }
    return false; // let native Android handle exit/minimize
  }

  public vibrate(ms: number = 25) {
    try {
      if (window.AndroidBridge?.vibrate) {
        window.AndroidBridge.vibrate(ms);
      } else if (navigator.vibrate) {
        navigator.vibrate(ms);
      }
    } catch (e) {
      // ignore
    }
  }

  public notify(title: string, body: string) {
    try {
      if (window.AndroidBridge?.postNotification) {
        window.AndroidBridge.postNotification(title, body);
      } else if ("Notification" in window && Notification.permission === "granted") {
        new Notification(title, { body, icon: "/public/assets/aistudio/logo.png" });
      }
    } catch (e) {
      // ignore
    }
  }
}

export const androidBridge = new AndroidBridgeManager();
