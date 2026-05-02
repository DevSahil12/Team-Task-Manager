import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { dashboardApi, tasksApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { statusBadge, priorityBadge } from '../utils/badges'

export default function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [recentTasks, setRecentTasks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([dashboardApi.getStats(), tasksApi.getMyTasks()])
      .then(([statsRes, tasksRes]) => {
        setStats(statsRes.data)
        setRecentTasks(tasksRes.data.slice(0, 5))
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="loading"><div className="spinner" /></div>

  const tasksPerUser = stats?.tasksPerUser || []

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Good day, {user?.name?.split(' ')[0]} 👋</h1>
          <p className="page-subtitle">Here's what's happening with your projects</p>
        </div>
        <div className="stat-pill">
          <span>📁</span> {stats?.projectCount ?? 0} project{stats?.projectCount !== 1 ? 's' : ''}
        </div>
      </div>

      {/* Stats row */}
      <div className="card-grid card-grid-4" style={{ marginBottom: 28 }}>
        <div className="card stat-card">
          <div className="stat-label">Total tasks</div>
          <div className="stat-value blue">{stats?.totalTasks ?? 0}</div>
        </div>
        <div className="card stat-card">
          <div className="stat-label">In progress</div>
          <div className="stat-value amber">{stats?.inProgressCount ?? 0}</div>
        </div>
        <div className="card stat-card">
          <div className="stat-label">Completed</div>
          <div className="stat-value green">{stats?.doneCount ?? 0}</div>
        </div>
        <div className="card stat-card">
          <div className="stat-label">Overdue</div>
          <div className="stat-value red">{stats?.overdueCount ?? 0}</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: tasksPerUser.length > 0 ? '1fr 320px' : '1fr', gap: 20 }}>
        {/* Recent tasks */}
        <div className="card">
          <div className="flex-between" style={{ marginBottom: 16 }}>
            <h2 style={{ fontSize: 16, fontWeight: 600 }}>My recent tasks</h2>
            <Link to="/tasks" className="btn btn-secondary btn-sm">View all</Link>
          </div>

          {recentTasks.length === 0 ? (
            <div className="empty">
              <div className="empty-icon">✓</div>
              <div className="empty-text">No tasks yet</div>
              <div className="empty-sub">Tasks assigned to you will appear here</div>
            </div>
          ) : (
            recentTasks.map(task => (
              <div key={task.id} className="task-card">
                <div className="task-card-header">
                  <div>
                    <div className="task-card-title">{task.title}</div>
                    <div className="text-muted">{task.projectName}</div>
                  </div>
                  <div className="task-badges">
                    {statusBadge(task.status)}
                    {priorityBadge(task.priority)}
                    {task.overdue && <span className="badge badge-overdue">Overdue</span>}
                  </div>
                </div>
                {task.dueDate && (
                  <div className="task-card-meta">
                    <span>Due: {new Date(task.dueDate).toLocaleDateString()}</span>
                  </div>
                )}
              </div>
            ))
          )}
        </div>

        {/* Tasks per user — shown to admins who manage projects */}
        {tasksPerUser.length > 0 && (
          <div className="card">
            <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>Tasks per user</h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {tasksPerUser.map((entry) => {
                const max = Math.max(...tasksPerUser.map(e => e.count))
                const pct = max > 0 ? (entry.count / max) * 100 : 0
                return (
                  <div key={entry.name}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
                      <span style={{ fontWeight: 500 }}>{entry.name}</span>
                      <span style={{ color: '#6b7280' }}>{entry.count} task{entry.count !== 1 ? 's' : ''}</span>
                    </div>
                    <div style={{ background: '#f3f4f6', borderRadius: 4, height: 6 }}>
                      <div style={{
                        width: `${pct}%`, height: '100%', background: '#6c63ff',
                        borderRadius: 4, transition: 'width 0.4s ease'
                      }} />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
