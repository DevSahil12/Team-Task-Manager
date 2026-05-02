import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api

// ─── Auth ──────────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
}

// ─── Users ─────────────────────────────────────────────────
export const usersApi = {
  searchByEmail: (email) => api.get('/users/search', { params: { email } }),
  me: () => api.get('/users/me'),
}

// ─── Projects ──────────────────────────────────────────────
export const projectsApi = {
  getAll: () => api.get('/projects'),
  getOne: (id) => api.get(`/projects/${id}`),
  create: (data) => api.post('/projects', data),
  getMembers: (id) => api.get(`/projects/${id}/members`),
  addMember: (id, data) => api.post(`/projects/${id}/members`, data),
  removeMember: (id, userId) => api.delete(`/projects/${id}/members/${userId}`),
}

// ─── Tasks ─────────────────────────────────────────────────
export const tasksApi = {
  getMyTasks: () => api.get('/tasks/my'),
  getByProject: (projectId) => api.get(`/tasks/project/${projectId}`),
  getOne: (id) => api.get(`/tasks/${id}`),
  create: (data) => api.post('/tasks', data),
  update: (id, data) => api.patch(`/tasks/${id}`, data),
  delete: (id) => api.delete(`/tasks/${id}`),
}

// ─── Dashboard ─────────────────────────────────────────────
export const dashboardApi = {
  getStats: () => api.get('/dashboard'),
}
