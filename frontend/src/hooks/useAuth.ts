import { reactive, readonly } from 'vue';
import { login as apiLogin, logout as apiLogout, getCurrentUser, type LoginRequest, type UserInfo } from '@/api/auth';

// Define the state structure
interface AuthState {
  isAuthenticated: boolean;
  user: UserInfo | null;
  token: string | null;
  refreshToken: string | null;
}

// Create a reactive state object. This is defined outside the hook function
// to ensure it's a singleton, shared across all components that use the hook.
const state = reactive<AuthState>({
  isAuthenticated: false,
  user: null,
  token: null,
  refreshToken: null,
});

// --- Actions ---
// These functions modify the shared state.

async function login(username: string, password: string): Promise<void> {
  try {
    const loginRequest: LoginRequest = {
      usernameOrEmail: username,
      password: password,
      rememberMe: false,
    };

    const response = await apiLogin(loginRequest);

    // Set state
    state.isAuthenticated = true;
    state.user = response.userInfo;
    state.token = response.accessToken;
    state.refreshToken = response.refreshToken;

    // Persist to localStorage
    localStorage.setItem('token', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
    localStorage.setItem('user', JSON.stringify(response.userInfo));

    console.log('Login successful:', response.userInfo);
  } catch (error: any) {
    // Clear state on failure
    state.isAuthenticated = false;
    state.user = null;
    state.token = null;
    state.refreshToken = null;

    console.error('Login failed:', error);
    throw error;
  }
}

function initializeAuth() {
  const token = localStorage.getItem('token');
  const refreshToken = localStorage.getItem('refreshToken');
  const userJson = localStorage.getItem('user');

  if (token && userJson) {
    try {
      state.token = token;
      state.refreshToken = refreshToken;
      state.user = JSON.parse(userJson);
      state.isAuthenticated = true;
      console.log('Authentication state initialized from localStorage');
    } catch (e) {
      console.error('Failed to parse user info from localStorage', e);
      // If parsing fails, treat as logged out
      logout();
    }
  }
}

async function logout() {
  try {
    // Call backend logout, but don't let it block clearing local state
    await apiLogout();
  } catch (error) {
    console.error('Backend logout call failed:', error);
  } finally {
    // Clear local state regardless of API call success
    state.isAuthenticated = false;
    state.user = null;
    state.token = null;
    state.refreshToken = null;
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    console.log('Logged out');
  }
}

async function fetchCurrentUser() {
  if (!state.isAuthenticated) {
    console.warn("Not authenticated, skipping fetchCurrentUser.");
    return;
  }
  try {
    const userInfo = await getCurrentUser();
    state.user = userInfo;
    localStorage.setItem('user', JSON.stringify(userInfo));
    return userInfo;
  } catch (error) {
    console.error('Failed to fetch current user:', error);
    // Potentially logout if token is invalid
    logout();
    throw error;
  }
}

// --- The Hook ---
// This is the function that components will call.
export function useAuth() {
  return {
    // Provide read-only access to state to prevent direct mutation from components
    state: readonly(state),
    // Provide actions to modify the state
    login,
    logout,
    initializeAuth,
    fetchCurrentUser,
  };
}
