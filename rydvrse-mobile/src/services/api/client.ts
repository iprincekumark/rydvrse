import { env } from "@/constants/env";
import { delay } from "@/utils/format";

type RequestOptions = {
  method?: "GET" | "POST" | "PATCH" | "PUT" | "DELETE";
  token?: string | null;
  body?: unknown;
  headers?: Record<string, string>;
};

const buildHeaders = (token?: string | null, extra?: Record<string, string>) => {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    Accept: "application/json",
    "X-Client-Platform": "MOBILE",
    "X-App-Version": "1.0.0",
    ...extra
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
};

export const apiClient = {
  async request<T>(path: string, options: RequestOptions = {}): Promise<T> {
    const response = await fetch(`${env.apiBaseUrl}${path}`, {
      method: options.method ?? "GET",
      headers: buildHeaders(options.token, options.headers),
      body: options.body ? JSON.stringify(options.body) : undefined
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || `Request failed with ${response.status}`);
    }

    const json = (await response.json()) as T;
    return json;
  },

  async simulate<T>(factory: () => T, latency = 550): Promise<T> {
    await delay(latency);
    return factory();
  }
};
