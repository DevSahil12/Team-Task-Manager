export function statusBadge(status) {
  const map = {
    TODO: ['badge-todo', 'To do'],
    IN_PROGRESS: ['badge-in-progress', 'In progress'],
    DONE: ['badge-done', 'Done'],
  }
  const [cls, label] = map[status] || ['badge-todo', status]
  return <span key={status} className={`badge ${cls}`}>{label}</span>
}

export function priorityBadge(priority) {
  const map = {
    LOW: ['badge-low', 'Low'],
    MEDIUM: ['badge-medium', 'Medium'],
    HIGH: ['badge-high', 'High'],
  }
  const [cls, label] = map[priority] || ['badge-medium', priority]
  return <span key={priority} className={`badge ${cls}`}>{label}</span>
}

export function roleBadge(role) {
  return (
    <span className={`badge ${role === 'ADMIN' ? 'badge-admin' : 'badge-member'}`}>
      {role === 'ADMIN' ? 'Admin' : 'Member'}
    </span>
  )
}
