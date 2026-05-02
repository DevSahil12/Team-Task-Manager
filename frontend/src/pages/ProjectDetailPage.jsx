import { useState, useEffect, useRef } from 'react'
import { useParams, Link } from 'react-router-dom'
import { projectsApi, tasksApi, usersApi } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { statusBadge, priorityBadge, roleBadge } from '../utils/badges'

const STATUS_OPTIONS = ['TODO', 'IN_PROGRESS', 'DONE']
const PRIORITY_OPTIONS = ['LOW', 'MEDIUM', 'HIGH']

export default function ProjectDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const [project, setProject] = useState(null)
  const [members, setMembers] = useState([])
  const [tasks, setTasks] = useState([])
  const [tab, setTab] = useState('tasks')
  const [loading, setLoading] = useState(true)

  // Task modal
  const [showTaskModal, setShowTaskModal] = useState(false)
  const [editingTask, setEditingTask] = useState(null)
  const [taskForm, setTaskForm] = useState({ title: '', description: '', dueDate: '', priority: 'MEDIUM', assignedToId: '' })
  const [taskError, setTaskError] = useState('')
  const [taskSubmitting, setTaskSubmitting] = useState(false)

  // Member modal
  const [showMemberModal, setShowMemberModal] = useState(false)
  const [memberEmail, setMemberEmail] = useState('')
  const [memberRole, setMemberRole] = useState('MEMBER')
  const [memberSearchResult, setMemberSearchResult] = useState(null)
  const [memberSearching, setMemberSearching] = useState(false)
  const [memberSearchError, setMemberSearchError] = useState('')
  const [memberSubmitting, setMemberSubmitting] = useState(false)
  const [memberError, setMemberError] = useState('')
  const searchTimeout = useRef(null)

  const isAdmin = project?.myRole === 'ADMIN'

  useEffect(() => {
    Promise.all([
      projectsApi.getOne(id),
      projectsApi.getMembers(id),
      tasksApi.getByProject(id),
    ]).then(([pRes, mRes, tRes]) => {
      setProject(pRes.data)
      setMembers(mRes.data)
      setTasks(tRes.data)
    }).catch(console.error).finally(() => setLoading(false))
  }, [id])

  // Live email search as user types
  const handleEmailChange = (val) => {
    setMemberEmail(val)
    setMemberSearchResult(null)
    setMemberSearchError('')
    clearTimeout(searchTimeout.current)
    if (!val.includes('@') || val.length < 5) return
    searchTimeout.current = setTimeout(async () => {
      setMemberSearching(true)
      try {
        const { data } = await usersApi.searchByEmail(val)
        setMemberSearchResult(data)
        setMemberSearchError('')
      } catch (err) {
        setMemberSearchResult(null)
        setMemberSearchError(err.response?.data?.error || 'User not found')
      } finally {
        setMemberSearching(false)
      }
    }, 600)
  }

  const handleAddMember = async (e) => {
    e.preventDefault()
    if (!memberSearchResult) {
      setMemberError('Please search and confirm a valid user first')
      return
    }
    if (members.find(m => m.userId === memberSearchResult.id)) {
      setMemberError('This user is already a member')
      return
    }
    setMemberSubmitting(true)
    setMemberError('')
    try {
      const { data } = await projectsApi.addMember(id, {
        userId: memberSearchResult.id,
        role: memberRole,
      })
      setMembers(prev => [...prev, data])
      setShowMemberModal(false)
      setMemberEmail('')
      setMemberSearchResult(null)
      setMemberRole('MEMBER')
    } catch (err) {
      setMemberError(err.response?.data?.error || 'Failed to add member')
    } finally {
      setMemberSubmitting(false)
    }
  }

  const handleRemoveMember = async (memberId) => {
    if (!confirm('Remove this member from the project?')) return
    try {
      await projectsApi.removeMember(id, memberId)
      setMembers(prev => prev.filter(m => m.userId !== memberId))
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to remove member')
    }
  }

  const handleCreateTask = async (e) => {
    e.preventDefault()
    setTaskError('')
    setTaskSubmitting(true)
    try {
      const payload = {
        ...taskForm,
        projectId: Number(id),
        assignedToId: taskForm.assignedToId ? Number(taskForm.assignedToId) : null,
        dueDate: taskForm.dueDate || null,
      }
      if (editingTask) {
        const { data } = await tasksApi.update(editingTask.id, payload)
        setTasks(prev => prev.map(t => t.id === data.id ? data : t))
      } else {
        const { data } = await tasksApi.create(payload)
        setTasks(prev => [data, ...prev])
      }
      closeTaskModal()
    } catch (err) {
      setTaskError(err.response?.data?.error || 'Failed to save task')
    } finally {
      setTaskSubmitting(false)
    }
  }

  const handleDeleteTask = async (taskId) => {
    if (!confirm('Delete this task?')) return
    try {
      await tasksApi.delete(taskId)
      setTasks(prev => prev.filter(t => t.id !== taskId))
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to delete task')
    }
  }

  const handleStatusChange = async (task, status) => {
    try {
      const { data } = await tasksApi.update(task.id, { status })
      setTasks(prev => prev.map(t => t.id === data.id ? data : t))
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to update status')
    }
  }

  const openEditTask = (task) => {
    setEditingTask(task)
    setTaskForm({
      title: task.title,
      description: task.description || '',
      dueDate: task.dueDate || '',
      priority: task.priority,
      assignedToId: task.assignedTo?.id || '',
    })
    setShowTaskModal(true)
  }

  const closeTaskModal = () => {
    setShowTaskModal(false)
    setEditingTask(null)
    setTaskForm({ title: '', description: '', dueDate: '', priority: 'MEDIUM', assignedToId: '' })
    setTaskError('')
  }

  const closeMemberModal = () => {
    setShowMemberModal(false)
    setMemberEmail('')
    setMemberSearchResult(null)
    setMemberSearchError('')
    setMemberError('')
    setMemberRole('MEMBER')
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>
  if (!project) return <div className="page"><div className="alert alert-error">Project not found</div></div>

  const todoTasks = tasks.filter(t => t.status === 'TODO')
  const inProgressTasks = tasks.filter(t => t.status === 'IN_PROGRESS')
  const doneTasks = tasks.filter(t => t.status === 'DONE')

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div style={{ fontSize: 13, color: '#6b7280', marginBottom: 4 }}>
            <Link to="/projects" style={{ color: '#6c63ff', textDecoration: 'none' }}>Projects</Link> / {project.name}
          </div>
          <h1 className="page-title">{project.name}</h1>
          {project.description && <p className="page-subtitle">{project.description}</p>}
        </div>
        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          {roleBadge(project.myRole)}
          {isAdmin && (
            <>
              <button className="btn btn-secondary btn-sm" onClick={() => setShowMemberModal(true)}>+ Add member</button>
              <button className="btn btn-primary btn-sm" onClick={() => setShowTaskModal(true)}>+ New task</button>
            </>
          )}
        </div>
      </div>

      {/* Stats row */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
        {[['To do', todoTasks.length, '#6b7280'], ['In progress', inProgressTasks.length, '#7c3aed'], ['Done', doneTasks.length, '#059669']].map(([label, count, color]) => (
          <div key={label} className="card" style={{ flex: 1, padding: '14px 20px' }}>
            <div style={{ fontSize: 12, color: '#6b7280', marginBottom: 4 }}>{label}</div>
            <div style={{ fontSize: 26, fontWeight: 700, color }}>{count}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 4, marginBottom: 20, borderBottom: '1px solid #e5e7eb' }}>
        {['tasks', 'members'].map(t => (
          <button key={t} onClick={() => setTab(t)} style={{
            padding: '8px 18px', border: 'none', background: 'none', cursor: 'pointer',
            fontSize: 14, fontWeight: tab === t ? 600 : 400,
            color: tab === t ? '#6c63ff' : '#6b7280',
            borderBottom: tab === t ? '2px solid #6c63ff' : '2px solid transparent',
            marginBottom: -1,
          }}>
            {t === 'tasks' ? `Tasks (${tasks.length})` : `Members (${members.length})`}
          </button>
        ))}
      </div>

      {/* Tasks tab */}
      {tab === 'tasks' && (
        tasks.length === 0 ? (
          <div className="card">
            <div className="empty">
              <div className="empty-icon">✓</div>
              <div className="empty-text">No tasks yet</div>
              {isAdmin && <div className="empty-sub">Create the first task for this project</div>}
              {!isAdmin && <div className="empty-sub">Tasks assigned to you will appear here</div>}
            </div>
          </div>
        ) : (
          tasks.map(task => {
            const isAssignee = task.assignedTo?.id === user?.id
            const canChangeStatus = isAdmin || isAssignee
            return (
              <div key={task.id} className="task-card">
                <div className="task-card-header">
                  <div style={{ flex: 1 }}>
                    <div className="task-card-title">{task.title}</div>
                    {task.description && <div className="text-muted" style={{ marginTop: 2 }}>{task.description}</div>}
                    <div className="task-badges" style={{ marginTop: 8 }}>
                      {statusBadge(task.status)}
                      {priorityBadge(task.priority)}
                      {task.overdue && <span className="badge badge-overdue">Overdue</span>}
                    </div>
                    <div className="task-card-meta">
                      {task.assignedTo
                        ? <span>👤 {task.assignedTo.name}</span>
                        : <span style={{ color: '#d1d5db' }}>Unassigned</span>}
                      {task.dueDate && <span>📅 {new Date(task.dueDate).toLocaleDateString()}</span>}
                      <span style={{ color: '#9ca3af' }}>by {task.createdBy?.name}</span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 6, alignItems: 'flex-end' }}>
                    {canChangeStatus && (
                      <select
                        className="form-select" style={{ width: 145, fontSize: 12, padding: '4px 8px' }}
                        value={task.status}
                        onChange={e => handleStatusChange(task, e.target.value)}
                      >
                        {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                      </select>
                    )}
                    {isAdmin && (
                      <div style={{ display: 'flex', gap: 6 }}>
                        <button className="btn btn-secondary btn-sm" onClick={() => openEditTask(task)}>Edit</button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDeleteTask(task.id)}>Delete</button>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            )
          })
        )
      )}

      {/* Members tab */}
      {tab === 'members' && (
        <div className="card">
          {members.length === 0 && (
            <div className="empty">
              <div className="empty-icon">👥</div>
              <div className="empty-text">No members yet</div>
            </div>
          )}
          {members.map(m => (
            <div key={m.userId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: '1px solid #f3f4f6' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div style={{
                  width: 36, height: 36, borderRadius: '50%', background: '#ede9fe',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 14, fontWeight: 600, color: '#7c3aed',
                }}>
                  {m.name?.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)}
                </div>
                <div>
                  <div style={{ fontWeight: 500, fontSize: 14 }}>{m.name}</div>
                  <div className="text-muted">{m.email}</div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                {roleBadge(m.role)}
                {isAdmin && m.userId !== user?.id && (
                  <button className="btn btn-danger btn-sm" onClick={() => handleRemoveMember(m.userId)}>Remove</button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Task modal */}
      {showTaskModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeTaskModal()}>
          <div className="modal">
            <h2 className="modal-title">{editingTask ? 'Edit task' : 'Create task'}</h2>
            {taskError && <div className="alert alert-error">{taskError}</div>}
            <form onSubmit={handleCreateTask}>
              <div className="form-group">
                <label className="form-label">Title *</label>
                <input type="text" className="form-input" value={taskForm.title}
                  onChange={e => setTaskForm({ ...taskForm, title: e.target.value })}
                  placeholder="What needs to be done?" required />
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea className="form-textarea" value={taskForm.description}
                  onChange={e => setTaskForm({ ...taskForm, description: e.target.value })}
                  placeholder="Add more details…" />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                <div className="form-group">
                  <label className="form-label">Due date</label>
                  <input type="date" className="form-input" value={taskForm.dueDate}
                    onChange={e => setTaskForm({ ...taskForm, dueDate: e.target.value })} />
                </div>
                <div className="form-group">
                  <label className="form-label">Priority</label>
                  <select className="form-select" value={taskForm.priority}
                    onChange={e => setTaskForm({ ...taskForm, priority: e.target.value })}>
                    {PRIORITY_OPTIONS.map(p => <option key={p} value={p}>{p}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Assign to</label>
                <select className="form-select" value={taskForm.assignedToId}
                  onChange={e => setTaskForm({ ...taskForm, assignedToId: e.target.value })}>
                  <option value="">— Unassigned —</option>
                  {members.map(m => <option key={m.userId} value={m.userId}>{m.name} ({m.email})</option>)}
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={closeTaskModal}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={taskSubmitting}>
                  {taskSubmitting ? 'Saving…' : editingTask ? 'Save changes' : 'Create task'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add member modal — search by email */}
      {showMemberModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && closeMemberModal()}>
          <div className="modal">
            <h2 className="modal-title">Add member</h2>
            {memberError && <div className="alert alert-error">{memberError}</div>}
            <form onSubmit={handleAddMember}>
              <div className="form-group">
                <label className="form-label">Search by email</label>
                <input
                  type="email" className="form-input"
                  placeholder="teammate@example.com"
                  value={memberEmail}
                  onChange={e => handleEmailChange(e.target.value)}
                />
                {memberSearching && (
                  <div style={{ fontSize: 12, color: '#6b7280', marginTop: 6 }}>Searching…</div>
                )}
                {memberSearchError && (
                  <div style={{ fontSize: 12, color: '#ef4444', marginTop: 6 }}>{memberSearchError}</div>
                )}
                {memberSearchResult && (
                  <div style={{
                    marginTop: 8, padding: '10px 14px', borderRadius: 8,
                    background: '#f0fdf4', border: '1px solid #a7f3d0',
                    display: 'flex', alignItems: 'center', gap: 10
                  }}>
                    <div style={{
                      width: 32, height: 32, borderRadius: '50%', background: '#6c63ff',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      color: '#fff', fontSize: 13, fontWeight: 600,
                    }}>
                      {memberSearchResult.name?.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()}
                    </div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: 14 }}>{memberSearchResult.name}</div>
                      <div style={{ fontSize: 12, color: '#6b7280' }}>{memberSearchResult.email}</div>
                    </div>
                    <span style={{ marginLeft: 'auto', fontSize: 18 }}>✅</span>
                  </div>
                )}
              </div>
              <div className="form-group">
                <label className="form-label">Role</label>
                <select className="form-select" value={memberRole} onChange={e => setMemberRole(e.target.value)}>
                  <option value="MEMBER">Member — view & update assigned tasks</option>
                  <option value="ADMIN">Admin — full control over tasks & members</option>
                </select>
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={closeMemberModal}>Cancel</button>
                <button type="submit" className="btn btn-primary"
                  disabled={memberSubmitting || !memberSearchResult}>
                  {memberSubmitting ? 'Adding…' : 'Add member'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
