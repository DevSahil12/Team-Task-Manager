import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { tasksApi } from '../api/client'
import { statusBadge, priorityBadge } from '../utils/badges'

const FILTER_OPTIONS = ['ALL', 'TODO', 'IN_PROGRESS', 'DONE']

export default function TasksPage() {
  const [tasks, setTasks] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    tasksApi.getMyTasks().then(r => setTasks(r.data)).finally(() => setLoading(false))
  }, [])

  const handleStatusChange = async (task, status) => {
    const { data } = await tasksApi.update(task.id, { status })
    setTasks(prev => prev.map(t => t.id === data.id ? data : t))
  }

  const filtered = filter === 'ALL' ? tasks : tasks.filter(t => t.status === filter)

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">My tasks</h1>
          <p className="page-subtitle">{tasks.length} task{tasks.length !== 1 ? 's' : ''} assigned to you</p>
        </div>
      </div>

      {/* Filter tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 20, flexWrap: 'wrap' }}>
        {FILTER_OPTIONS.map(f => (
          <button key={f} onClick={() => setFilter(f)}
            className={`btn btn-sm ${filter === f ? 'btn-primary' : 'btn-secondary'}`}>
            {f === 'ALL' ? 'All' : f.replace('_', ' ')}
            {f !== 'ALL' && (
              <span style={{ marginLeft: 4, opacity: 0.7 }}>
                ({tasks.filter(t => t.status === f).length})
              </span>
            )}
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="card">
          <div className="empty">
            <div className="empty-icon">✓</div>
            <div className="empty-text">No {filter !== 'ALL' ? filter.replace('_', ' ').toLowerCase() + ' ' : ''}tasks</div>
            <div className="empty-sub">Tasks assigned to you will appear here</div>
          </div>
        </div>
      ) : (
        filtered.map(task => (
          <div key={task.id} className="task-card">
            <div className="task-card-header">
              <div style={{ flex: 1 }}>
                <div className="task-card-title">{task.title}</div>
                {task.description && (
                  <div className="text-muted" style={{ marginTop: 2, marginBottom: 6 }}>
                    {task.description.length > 100 ? task.description.slice(0, 100) + '…' : task.description}
                  </div>
                )}
                <div className="task-badges">
                  {statusBadge(task.status)}
                  {priorityBadge(task.priority)}
                  {task.overdue && <span className="badge badge-overdue">Overdue</span>}
                </div>
                <div className="task-card-meta">
                  <span>
                    <Link to={`/projects/${task.projectId}`} style={{ color: '#6c63ff', textDecoration: 'none' }}>
                      {task.projectName}
                    </Link>
                  </span>
                  {task.dueDate && <span>Due: {new Date(task.dueDate).toLocaleDateString()}</span>}
                </div>
              </div>
              <div>
                <select
                  className="form-select" style={{ width: 150, fontSize: 13, padding: '6px 10px' }}
                  value={task.status}
                  onChange={e => handleStatusChange(task, e.target.value)}
                >
                  <option value="TODO">To do</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  )
}
