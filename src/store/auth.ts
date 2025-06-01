import { defineStore } from 'pinia'

interface MockUser {
  id: string;
  username: string;
  name: string;
  role: string;
  department: string;
  email: string;
  phone: string;
  avatar: string;
  permissions: string[];
}

interface AuthState {
  isAuthenticated: boolean;
  user: MockUser | null;
  token: string | null;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    isAuthenticated: false,
    user: null,
    token: null
  }),
  actions: {
    async login(username: string, password: string): Promise<void> {
      // Simulate API call based on frontend-mock-auth-specification.md
      console.log(`Attempting to login with username: ${username} and password: ${password}`);

      // --- Mock Login Logic (replace with actual API call later) ---
      // Find user in mockUsers (from frontend-mock-auth-specification.md)
      const mockUsers = [
        {
          id: "admin001",
          username: "admin",
          name: "系统管理员",
          role: "ADMIN",
          department: "系统管理部",
          email: "admin@example.com",
          phone: "13800000001",
          avatar: "/avatars/admin.png",
          permissions: ["*"]
        },
        {
          id: "pm001",
          username: "projectmanager",
          name: "张项目",
          role: "PROJECT_MANAGER",
          department: "项目管理部",
          email: "pm@example.com",
          phone: "13800000002",
          avatar: "/avatars/pm.png",
          permissions: [
            "project:create",
            "project:edit",
            "project:delete",
            "project:view",
            "team:manage",
            "document:review"
          ]
        },
        {
          id: "bid001",
          username: "bidspecialist",
          name: "李投标",
          role: "BID_SPECIALIST",
          department: "投标部",
          email: "bid@example.com",
          phone: "13800000003",
          avatar: "/avatars/bid.png",
          permissions: [
            "project:view",
            "document:edit",
            "document:submit",
            "document:view"
          ]
        }
      ];

      const foundUser = mockUsers.find(user => user.username === username);

      if (foundUser) {
        // Simulate successful login
        this.isAuthenticated = true;
        this.user = foundUser;
        // Simulate token generation
        this.token = `mock-token-${foundUser.id}-${Date.now()}`;
        console.log('Login successful!', this.user, this.token);

        // Store in localStorage (as per specification)
        localStorage.setItem('token', this.token);
        localStorage.setItem('user', JSON.stringify(this.user));

      } else {
        // Simulate failed login
        this.isAuthenticated = false;
        this.user = null;
        this.token = null;
        console.error('Login failed: User not found');
        // You might want to throw an error or handle this case appropriately in the UI
        throw new Error('Invalid username or password');
      }
      // --- End Mock Login Logic ---
    },

    // Action to check local storage on app initialization
    initializeAuth() {
        const token = localStorage.getItem('token');
        const user = localStorage.getItem('user');

        if (token && user) {
            try {
                this.token = token;
                this.user = JSON.parse(user);
                this.isAuthenticated = true;
                console.log('Auth initialized from localStorage');
            } catch (e) {
                console.error('Failed to parse user from localStorage', e);
                this.logout(); // Clear invalid data
            }
        }
    },

    // Action to log out
    logout() {
        this.isAuthenticated = false;
        this.user = null;
        this.token = null;
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        console.log('Logged out');
    }
  }
}); 