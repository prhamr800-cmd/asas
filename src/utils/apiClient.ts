// Centralized API Client and Network Interceptor for Privo
// Supports both Web and Native Android App runtimes

declare global {
  interface Window {
    AndroidBridge?: {
      getApiBaseUrl?: () => string;
      setApiBaseUrl?: (url: string) => void;
      getSecureItem?: (key: string) => string | null;
      setSecureItem?: (key: string, value: string) => void;
      removeSecureItem?: (key: string) => void;
      requestPermission?: (permission: string) => boolean;
      vibrate?: (ms: number) => void;
      onBackPressed?: () => boolean;
      postNotification?: (title: string, body: string) => void;
    };
    __PRIVO_API_BASE_URL__?: string;
  }
}

const DEFAULT_REMOTE_BACKEND = "https://ais-dev-czkhglys2igce2wspciorl-436229882791.us-west1.run.app";

export function isAndroidApp(): boolean {
  return typeof window !== "undefined" && (
    !!window.AndroidBridge ||
    window.location.hostname === "appassets.androidplatform.net" ||
    window.location.protocol === "file:" ||
    navigator.userAgent.includes("PrivoAndroid")
  );
}

export function getApiBaseUrl(): string {
  if (typeof window === "undefined") return "";

  // 1. Explicit window override
  if (window.__PRIVO_API_BASE_URL__) {
    return window.__PRIVO_API_BASE_URL__.replace(/\/$/, "");
  }

  // 2. Android Bridge override
  try {
    const bridgeUrl = window.AndroidBridge?.getApiBaseUrl?.();
    if (bridgeUrl && bridgeUrl.trim().length > 0) {
      return bridgeUrl.replace(/\/$/, "");
    }
  } catch (e) {
    // ignore
  }

  // 3. User configured in settings / localStorage
  const savedUrl = localStorage.getItem("privo_api_base_url");
  if (savedUrl && savedUrl.trim().length > 0) {
    return savedUrl.replace(/\/$/, "");
  }

  // 4. If running inside Android WebView or standalone app, default to the live backend URL
  if (isAndroidApp() || window.location.hostname === "localhost" && window.location.port !== "3000") {
    return DEFAULT_REMOTE_BACKEND;
  }

  // 5. Running in standard web browser served by Node backend
  return "";
}

export function setApiBaseUrl(url: string): void {
  const clean = url.trim().replace(/\/$/, "");
  localStorage.setItem("privo_api_base_url", clean);
  if (window.AndroidBridge?.setApiBaseUrl) {
    window.AndroidBridge.setApiBaseUrl(clean);
  }
}

export function getWebSocketUrl(): string {
  const base = getApiBaseUrl();
  if (base) {
    return base.replace(/^http/, "ws");
  }
  const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
  return `${protocol}//${window.location.host}`;
}

// Global fetch wrapper to seamlessly route /api calls to the target backend
let isFetchIntercepted = false;

export function initApiClient() {
  if (typeof window === "undefined" || isFetchIntercepted) return;
  isFetchIntercepted = true;

  const originalFetch = window.fetch.bind(window);

  window.fetch = async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
    let url = typeof input === "string" ? input : input instanceof URL ? input.toString() : input.url;
    const base = getApiBaseUrl();

    // Check if this is an API call
    if (url.startsWith("/api/") || url.startsWith("/uploads/") || url.startsWith("/api")) {
      if (base) {
        url = `${base}${url}`;
      }
    }

    // Attach Session ID if available
    const sessionId = localStorage.getItem("parham_session_id") || 
                      window.AndroidBridge?.getSecureItem?.("parham_session_id") || null;
    
    let newInit = init || {};
    if (sessionId) {
      const headers = new Headers(newInit.headers || {});
      if (!headers.has("X-Session-Id")) {
        headers.set("X-Session-Id", sessionId);
      }
      newInit = { ...newInit, headers };
    }

    try {
      const response = await originalFetch(url, newInit);
      return response;
    } catch (err) {
      console.warn(`[Privo API Client] Fetch error for ${url}:`, err);
      throw err;
    }
  };

  console.log(`[Privo API Client] Initialized. Target Base: "${getApiBaseUrl() || window.location.origin}" | IsAndroid: ${isAndroidApp()}`);
}

// Ensure init runs immediately on import
initApiClient();
