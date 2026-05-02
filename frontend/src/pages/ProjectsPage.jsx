import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { projectsApi } from '../api/client'
import { roleBadge } from '../utils/badges'

export default function ProjectsPage() {
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [form, setForm] = useState({ name: '', description: '' })
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    projectsApi.getAll().then(r => setProjects(r.data)).finally(() => setLoading(false))
  }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreating(true)
    setError('')
    try {
      const { data } = await projectsApi.create(form)
      setProjects(prev => [data, ...prev])
      setShowModal(false)
      setForm({ name: '', description: '' })
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to create project')
    } finally {
      setCreating(false)
    }
  }

  if (loading) return <div className="loading"><div className="spinner" /></div>

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Projects</h1>
          <p className="page-subtitle">{projects.length} project{projects.length !== 1 ? 's' : ''} you're part of</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>+ New project</button>
      </div>

      {projects.length === 0 ? (
        <div className="card">
          <div className="empty">
            <div className="empty-icon">◫</div>
            <div className="empty-text">No projects yet</div>
            <div className="empty-sub">Create your first project to get started</div>
          </div>
        </div>
      ) : (
        <div className="card-grid card-grid-2">
          {projects.map(p => (
            <Link to={`/projects/${p.id}`} key={p.id} className="project-card">
              <div className="project-card-name">{p.name}</div>
              <div className="project-card-desc">{p.description || 'No description'}</div>
              <div className="project-card-stats">
                <span>👥 {p.memberCount} member{p.memberCount !== 1 ? 's' : ''}</span>
                <span>✓ {p.taskCount} task{p.taskCount !== 1 ? 's' : ''}</span>
              </div>
              <div className="project-card-role">{roleBadge(p.myRole)}</div>
            </Link>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowModal(false)}>
          <div className="modal">
            <h2 className="modal-title">Create new project</h2>
            {error && <div className="alert alert-error">{error}</div>}
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label className="form-label">Project name</label>
                <input
                  type="text" className="form-input" placeholder="e.g. Website Redesign"
                  value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Description (optional)</label>
                <textarea
                  className="form-textarea" placeholder="What's this project about?"
                  value={form.description} onChange={e => setForm({ ...form, description: e.target.value })}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={creating}>
                  {creating ? 'Creating…' : 'Create project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
