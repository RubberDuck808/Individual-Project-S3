import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import {
  register,
  login,
  authFetch,
  fetchCurrentUser,
  logout,
  getStoredUser,
  isLoggedIn,
  getAuthHeader,
  getStoredUserId,
} from "../../src/api/auth";

// Mock environment variable
vi.stubGlobal("import", {
  meta: {
    env: {
      VITE_API_URL: "http://localhost:8080",
    },
  },
});

describe("auth API", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    global.fetch = vi.fn();
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe("register", () => {
    it("should register a new user successfully", async () => {
      const mockResponse = { id: 1, username: "testuser", email: "test@example.com" };
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await register("testuser", "test@example.com", "password123", "Test User");

      expect(global.fetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/users/register",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            username: "testuser",
            email: "test@example.com",
            password: "password123",
            name: "Test User",
          }),
        }
      );
      expect(result).toEqual(mockResponse);
    });

    it("should throw error on failed registration", async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        headers: {
          get: () => "application/json",
        },
        json: async () => ({ error: "Email already exists" }),
      });

      await expect(
        register("testuser", "test@example.com", "password123", "Test User")
      ).rejects.toThrow("Email already exists");
    });
  });

  describe("login", () => {
    it("should login successfully and store token", async () => {
      const mockResponse = {
        token: "test-token-123",
        user: { id: 1, username: "testuser", email: "test@example.com" },
      };
      global.fetch.mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await login("test@example.com", "password123");

      expect(localStorage.getItem("token")).toBe("test-token-123");
      expect(localStorage.getItem("user")).toBe(JSON.stringify(mockResponse.user));
      expect(result).toEqual(mockResponse);
    });

    it("should throw error on failed login", async () => {
      global.fetch.mockResolvedValueOnce({
        ok: false,
        headers: {
          get: () => "application/json",
        },
        json: async () => ({ error: "Invalid credentials" }),
      });

      await expect(login("test@example.com", "wrongpassword")).rejects.toThrow(
        "Invalid credentials"
      );
      expect(localStorage.getItem("token")).toBeNull();
    });
  });

  describe("authFetch", () => {
    it("should make authenticated request with token", async () => {
      localStorage.setItem("token", "test-token");
      const mockResponse = { data: "test" };
      global.fetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: () => "application/json",
        },
        json: async () => mockResponse,
      });

      const result = await authFetch("/api/test");

      expect(global.fetch).toHaveBeenCalledWith(
        "http://localhost:8080/api/test",
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: "Bearer test-token",
          },
        }
      );
      expect(result).toEqual(mockResponse);
    });

    it("should throw error if not authenticated", async () => {
      await expect(authFetch("/api/test")).rejects.toThrow("Not authenticated");
    });

    it("should handle 204 No Content response", async () => {
      localStorage.setItem("token", "test-token");
      global.fetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: {
          get: () => "",
        },
      });

      const result = await authFetch("/api/test", { method: "DELETE" });
      expect(result).toBeNull();
    });

    it("should handle text response", async () => {
      localStorage.setItem("token", "test-token");
      global.fetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: () => "text/plain",
        },
        text: async () => "text response",
      });

      const result = await authFetch("/api/test");
      expect(result).toBe("text response");
    });

    it("should throw error on failed request", async () => {
      localStorage.setItem("token", "test-token");
      global.fetch.mockResolvedValueOnce({
        ok: false,
        status: 404,
        headers: {
          get: () => "application/json",
        },
        json: async () => ({ error: "Not found" }),
      });

      await expect(authFetch("/api/test")).rejects.toThrow("Not found");
    });
  });

  describe("fetchCurrentUser", () => {
    it("should fetch and store current user", async () => {
      localStorage.setItem("token", "test-token");
      const mockUser = { id: 1, username: "testuser" };
      global.fetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: () => "application/json",
        },
        json: async () => mockUser,
      });

      const result = await fetchCurrentUser();

      expect(localStorage.getItem("user")).toBe(JSON.stringify(mockUser));
      expect(result).toEqual(mockUser);
    });
  });

  describe("logout", () => {
    it("should clear token and user from localStorage", () => {
      localStorage.setItem("token", "test-token");
      localStorage.setItem("user", JSON.stringify({ id: 1 }));

      logout();

      expect(localStorage.getItem("token")).toBeNull();
      expect(localStorage.getItem("user")).toBeNull();
    });
  });

  describe("getStoredUser", () => {
    it("should return parsed user from localStorage", () => {
      const user = { id: 1, username: "testuser" };
      localStorage.setItem("user", JSON.stringify(user));

      expect(getStoredUser()).toEqual(user);
    });

    it("should return null if no user in localStorage", () => {
      expect(getStoredUser()).toBeNull();
    });

    it("should return null if invalid JSON in localStorage", () => {
      localStorage.setItem("user", "invalid json");
      expect(getStoredUser()).toBeNull();
    });
  });

  describe("isLoggedIn", () => {
    it("should return true if token exists", () => {
      localStorage.setItem("token", "test-token");
      expect(isLoggedIn()).toBe(true);
    });

    it("should return false if no token", () => {
      expect(isLoggedIn()).toBe(false);
    });
  });

  describe("getAuthHeader", () => {
    it("should return auth header with token", () => {
      localStorage.setItem("token", "test-token");
      expect(getAuthHeader()).toEqual({ Authorization: "Bearer test-token" });
    });

    it("should throw error if no token", () => {
      expect(() => getAuthHeader()).toThrow("Not authenticated");
    });
  });

  describe("getStoredUserId", () => {
    it("should return user id from stored user", () => {
      localStorage.setItem("user", JSON.stringify({ id: 123 }));
      expect(getStoredUserId()).toBe(123);
    });

    it("should return userId if id is not present", () => {
      localStorage.setItem("user", JSON.stringify({ userId: 456 }));
      expect(getStoredUserId()).toBe(456);
    });

    it("should return null if no user stored", () => {
      expect(getStoredUserId()).toBeNull();
    });
  });
});
