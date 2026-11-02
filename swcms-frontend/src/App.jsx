
import { useEffect, useMemo, useState } from 'react'
import API from './api'
import './App.css'
import ComplaintPhoto from './components/ComplaintPhoto'
import WorkerCompletionForm from './components/WorkerCompletionForm'
import FeedbackPhoto from './components/FeedbackPhoto'
import WorkerFeedbackForm from './components/WorkerFeedbackForm'
import WorkerFeedbackDisplay from './components/WorkerFeedbackDisplay'
import CompletionPhoto from './components/CompletionPhoto'
import Pagination from './components/Pagination'

const initialForm = {
  customerName: '',
  address: '',
  complaintType: '',
  description: '',
  priority: 'Medium'
}

const priorityOptions = ['Low', 'Medium', 'High']
const statusOptions = ['Pending', 'Assigned', 'In Progress', 'Resolved', 'Cancelled', 'Rejected']

const emptyAccountForm = {
  name: '',
  email: '',
  password: ''
}

function getRoleFromToken(token) {
  try {
    const payloadPart = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')

    const paddedPayload = payloadPart.padEnd(
      payloadPart.length + ((4 - payloadPart.length % 4) % 4),
      '='
    )

    const claims = JSON.parse(atob(paddedPayload))
    const value = claims.role || claims.roles || claims.authorities || ''

    const roleValue = String(
      Array.isArray(value) ? value.join(',') : value
    ).toUpperCase()

    if (roleValue.includes('ADMIN')) return 'ADMIN'
    if (roleValue.includes('WORKER')) return 'WORKER'
    if (roleValue.includes('USER')) return 'USER'

    return null
  } catch {
    return null
  }
}

function App() {
  // AUTHENTICATION
  const [token, setToken] = useState(() => localStorage.getItem('token'))
  const [role, setRole] = useState(() => localStorage.getItem('role'))
  const [workerCitizenFeedbacks, setWorkerCitizenFeedbacks] = useState([])
  const [authPage, setAuthPage] = useState('login')
  const [loginRole, setLoginRole] = useState('USER')

  const [authForm, setAuthForm] = useState({
    name: '',
    email: '',
    password: ''
  })

  const [authLoading, setAuthLoading] = useState(false)
  const [complaintPhoto, setComplaintPhoto] = useState(null);
  const isAdmin = role === 'ADMIN'
  const isWorker = role === 'WORKER'
  const isCitizen = role === 'USER'

  // MAIN STATE
  const [complaints, setComplaints] = useState([])
  const [workers, setWorkers] = useState([])
  const [workerEdit, setWorkerEdit] = useState(null)
  const [selectedWorkerId, setSelectedWorkerId] = useState(null)
  const [selectedWorkerComplaints, setSelectedWorkerComplaints] = useState([])
  const [workerStats, setWorkerStats] = useState(null)
  const [notifications, setNotifications] = useState([])
  const [overdueIds, setOverdueIds] = useState([])
  const [activity, setActivity] = useState([])
  const [profile, setProfile] = useState(null)
  const [profileForm, setProfileForm] = useState({ name: '', email: '' })
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' })
  const [selectedWorkers, setSelectedWorkers] = useState({})

  const [page, setPage] = useState('dashboard')
  const [form, setForm] = useState(initialForm)

  const [adminForm, setAdminForm] = useState(emptyAccountForm)
  const [workerForm, setWorkerForm] = useState(emptyAccountForm)

  const [creatingAdmin, setCreatingAdmin] = useState(false)
  const [creatingWorker, setCreatingWorker] = useState(false)

  // FEEDBACK STATE
  const [feedbacks, setFeedbacks] = useState([])
  const [workerFeedbacks, setWorkerFeedbacks] = useState([])
  const [feedbackDrafts, setFeedbackDrafts] = useState({})
  const [feedbackLoading, setFeedbackLoading] = useState(false)
  const [submittingFeedbackId, setSubmittingFeedbackId] = useState(null)
  const [feedbackPhotos, setFeedbackPhotos] = useState({})
  const [feedbackPage, setFeedbackPage] = useState(1)
  const [workerFeedbackPage, setWorkerFeedbackPage] = useState(1)
  const [workerCitizenFeedbackPage, setWorkerCitizenFeedbackPage] = useState(1)
  const [feedbackPageSize, setFeedbackPageSize] = useState(10)

  // FILTERS
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('All')
  const [typeFilter, setTypeFilter] = useState('All')
  const [priorityFilter, setPriorityFilter] = useState('All')
  const [sortBy, setSortBy] = useState('date-desc')
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)

  // MODAL AND COMPLAINT HISTORY
const [selectedComplaint, setSelectedComplaint] = useState(null)
const [complaintHistory, setComplaintHistory] = useState([])
const [historyLoading, setHistoryLoading] = useState(false)
const [historyError, setHistoryError] = useState('')

  // MESSAGES
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  // LOADING STATES
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [updatingId, setUpdatingId] = useState(null)
  const [assigningId, setAssigningId] = useState(null)
  const [deletingId, setDeletingId] = useState(null)

  // HELPERS
  const showMessage = text => {
    setMessage(text)
    setError('')
  }

  const showError = text => {
    setError(text)
    setMessage('')
  }

  const getErrorMessage = (err, fallback) => {
    if (err?.code === 'ECONNABORTED') return 'The server took too long to respond. Please try again.'
    if (err?.message === 'Network Error' || !err?.response) {
      return 'Cannot connect to SWCMS. Start the backend and try again.'
    }
    const data = err.response?.data

    if (typeof data === 'string') return data
    if (data?.message) return data.message
    if (data?.error) return data.error

    if (data?.fieldErrors && typeof data.fieldErrors === 'object') {
      return Object.values(data.fieldErrors).join(', ')
    }
    if (data?.errors && typeof data.errors === 'object') {
      return Object.values(data.errors).join(', ')
    }

    return fallback
  }

  // AUTH INPUT
  const handleAuthInput = event => {
    const { name, value } = event.target

    setAuthForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  // LOGOUT
  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')

    setToken(null)
    setRole(null)
    setComplaints([])
    setWorkers([])
    setWorkerStats(null)
    setNotifications([])
    setProfile(null)
    setOverdueIds([])
    setActivity([])
    setSelectedWorkers({})
    setFeedbacks([])
    setFeedbackDrafts({})
    setSelectedComplaint(null)
    setPage('dashboard')
    setMessage('')
    setError('')
    setAuthForm({ name: '', email: '', password: '' })
    setAuthPage('login')
  }

  // REGISTER CITIZEN
  const handleRegister = async event => {
    event.preventDefault()

    setAuthLoading(true)
    setMessage('')
    setError('')

    try {
      await API.post('/auth/register', authForm)

      setAuthForm({
        name: '',
        email: authForm.email,
        password: ''
      })

      setAuthPage('login')
      setLoginRole('USER')

      showMessage('Registration successful. Please log in.')
    } catch (err) {
      showError(
        getErrorMessage(err, 'Registration failed. Check your details.')
      )
    } finally {
      setAuthLoading(false)
    }
  }

  // LOGIN
  const handleLogin = async event => {
    event.preventDefault()

    setAuthLoading(true)
    setMessage('')
    setError('')

    try {
      const response = await API.post('/auth/login', {
        email: authForm.email,
        password: authForm.password
      })

      const newToken = response.data?.token

      if (!newToken) {
        throw new Error('The server did not return a token.')
      }

      const detectedRole = getRoleFromToken(newToken)

      if (!detectedRole || detectedRole !== loginRole) {
        showError(`This account is not authorized for ${loginRole} login.`)
        return
      }

      localStorage.setItem('token', newToken)
      localStorage.setItem('role', detectedRole)

      setToken(newToken)
      setRole(detectedRole)

      setAuthForm({
        name: '',
        email: '',
        password: ''
      })

      setPage('dashboard')

      showMessage(
        `Welcome ${
          detectedRole === 'ADMIN'
            ? 'Admin'
            : detectedRole === 'WORKER'
              ? 'Worker'
              : 'Citizen'
        }!`
      )
    } catch (err) {
      showError(
        getErrorMessage(err, 'Login failed. Check your email and password.')
      )
    } finally {
      setAuthLoading(false)
    }
  }

  // FETCH COMPLAINTS BASED ON ROLE
  const fetchComplaints = async () => {
    if (!token || !role) return null

    setLoading(true)
    setError('')

    try {
      let endpoint = '/user/complaints'

      if (isAdmin) {
        endpoint = '/admin/complaints'
      } else if (isWorker) {
        endpoint = '/worker/complaints'
      }

      const response = await API.get(endpoint)

      const data = Array.isArray(response.data)
        ? response.data
        : []

      setComplaints(data)

      return data
    } catch (err) {
      console.error('Fetch complaints error:', err)

      showError(
        getErrorMessage(
          err,
          'Unable to load complaints. Check whether the backend is running.'
        )
      )

      return null
    } finally {
      setLoading(false)
    }
  }

  // FETCH WORKERS FOR ADMIN
  const fetchWorkers = async () => {
    if (!token || !isAdmin) return

    try {
      const response = await API.get('/admin/workers')

      setWorkers(
        Array.isArray(response.data) ? response.data : []
      )
    } catch (err) {
      console.error('Fetch workers error:', err)

      showError(
        getErrorMessage(err, 'Unable to load workers.')
      )
    }
  }

  const fetchWorkerStats = async () => {
    if (!token || !isWorker) return
    try { const response = await API.get('/worker/stats'); setWorkerStats(response.data) }
    catch (err) { showError(getErrorMessage(err, 'Unable to load your work statistics.')) }
  }

  const fetchProfile = async () => {
    if (!token || isAdmin) return
    try {
      const response = await API.get(isWorker ? '/worker/profile' : '/user/profile')
      setProfile(response.data)
      setProfileForm({ name: response.data.name, email: response.data.email })
    } catch (err) { showError(getErrorMessage(err, 'Unable to load your profile.')) }
  }

  const fetchNotifications = async () => {
    if (!token || !isCitizen) return
    try { const response = await API.get('/user/notifications'); setNotifications(Array.isArray(response.data) ? response.data : []) }
    catch (err) { console.warn('Notification refresh failed', err) }
  }

  const fetchOverdue = async () => {
    if (!token || !isAdmin) return
    try { const response = await API.get('/admin/complaints/overdue'); setOverdueIds((response.data || []).map(item => item.complaintId)) }
    catch (err) { console.warn('Overdue complaint refresh failed', err) }
  }

  const fetchActivity = async () => {
    if (!token || !isAdmin) return
    try { const response = await API.get('/admin/activity'); setActivity(Array.isArray(response.data) ? response.data : []) }
    catch (err) { console.warn('Activity refresh failed', err) }
  }

  const handleCreateAdmin = async event => {
    event.preventDefault(); setCreatingAdmin(true)
    try { await API.post('/auth/admin/register', adminForm); setAdminForm(emptyAccountForm); showMessage('Admin account created successfully.') }
    catch (err) { showError(getErrorMessage(err,'Unable to create admin account.')) }
    finally { setCreatingAdmin(false) }
  }

  const handleCreateWorker = async event => {
    event.preventDefault(); setCreatingWorker(true)
    try { await API.post('/auth/worker/register', workerForm); setWorkerForm(emptyAccountForm); await fetchWorkers(); showMessage('Worker account created successfully.') }
    catch (err) { showError(getErrorMessage(err,'Unable to create worker account.')) }
    finally { setCreatingWorker(false) }
  }

  const saveProfile = async event => {
    event.preventDefault()
    try {
      const path = isWorker ? '/worker/profile' : '/user/profile'
      const response = await API.put(path, profileForm)
      setProfile(response.data.profile)
      localStorage.setItem('token', response.data.token)
      setToken(response.data.token)
      showMessage('Profile updated successfully.')
    } catch (err) { showError(getErrorMessage(err, 'Unable to update your profile.')) }
  }

  const changePassword = async event => {
    event.preventDefault()
    try {
      await API.put(isWorker ? '/worker/password' : '/user/password', passwordForm)
      setPasswordForm({ currentPassword: '', newPassword: '' })
      showMessage('Password changed successfully.')
    } catch (err) { showError(getErrorMessage(err, 'Unable to change password.')) }
  }

  const saveWorker = async event => {
    event.preventDefault()
    try {
      const response = await API.put(`/admin/workers/${workerEdit.userId}`, workerEdit)
      setWorkers(previous => previous.map(worker => worker.userId === response.data.userId ? response.data : worker))
      setWorkerEdit(null)
      showMessage('Worker details updated.')
    } catch (err) { showError(getErrorMessage(err, 'Unable to update worker.')) }
  }

  const escalateOverdue = async () => {
    try {
      const response = await API.post('/admin/complaints/escalate-overdue')
      await fetchComplaints()
      await fetchOverdue()
      showMessage(`${response.data.escalated} overdue complaint${response.data.escalated === 1 ? '' : 's'} escalated.`)
    } catch (err) { showError(getErrorMessage(err, 'Unable to escalate overdue complaints.')) }
  }

  // FETCH FEEDBACK
  // FETCH COMPLAINT STATUS HISTORY
const fetchComplaintHistory = async complaintId => {
  if (!complaintId) return

  setHistoryLoading(true)
  setHistoryError('')
  setComplaintHistory([])

  try {
    let endpoint = `/user/complaints/${complaintId}/history`

    if (isAdmin) {
      endpoint = `/admin/complaints/${complaintId}/history`
    } else if (isWorker) {
      endpoint = `/worker/complaints/${complaintId}/history`
    }

    const response = await API.get(endpoint)

    setComplaintHistory(
      Array.isArray(response.data) ? response.data : []
    )
  } catch (err) {
    console.error('Complaint history error:', err)

    setHistoryError(
      getErrorMessage(err, 'Unable to load complaint history.')
    )
  } finally {
    setHistoryLoading(false)
  }
}
  const fetchFeedback = async () => {
    if (!token || (!isAdmin && !isCitizen)) return

    setFeedbackLoading(true)

    try {
      const endpoint = isAdmin
        ? '/admin/feedback'
        : '/user/feedback'

      const response = await API.get(endpoint)

      setFeedbacks(
        Array.isArray(response.data) ? response.data : []
      )
      setFeedbackPage(1)
      if (isAdmin) {
        const workerResponse = await API.get('/admin/worker-feedback')
        setWorkerFeedbacks(Array.isArray(workerResponse.data) ? workerResponse.data : [])
        setWorkerFeedbackPage(1)
      }
    } catch (err) {
      console.error('Fetch feedback error:', err)

      showError(
        getErrorMessage(err, 'Unable to load feedback.')
      )
    } finally {
      setFeedbackLoading(false)
    }
  }
  // FETCH CITIZEN FEEDBACK FOR ASSIGNED WORKER
const fetchWorkerCitizenFeedback = async () => {
  if (!token || !isWorker) return

  try {
    const response = await API.get('/worker/feedback')

    setWorkerCitizenFeedbacks(
      Array.isArray(response.data) ? response.data : []
    )
    setWorkerCitizenFeedbackPage(1)
  } catch (err) {
    console.error('Fetch worker feedback error:', err)
    showError(
      getErrorMessage(err, 'Unable to load citizen feedback.')
    )
  }
}
  const handleFeedbackSubmit = async (event, complaintId) => {
  event.preventDefault()

  setSubmittingFeedbackId(complaintId)
  setMessage('')
  setError('')

  try {
    const draft = feedbackDrafts[complaintId] || {
      rating: 5,
      comment: ''
    }

    const formData = new FormData()
    formData.append('rating', Number(draft.rating))
    formData.append('comment', draft.comment)

    if (feedbackPhotos[complaintId]) {
      formData.append('photo', feedbackPhotos[complaintId])
    }

    await API.post(
      `/user/complaints/${complaintId}/feedback`,
      formData
    )

    setFeedbackPhotos(previous => ({
      ...previous,
      [complaintId]: null
    }))

    setFeedbackDrafts(previous => {
      const updated = { ...previous }
      delete updated[complaintId]
      return updated
    })

    await fetchFeedback()
    showMessage('Feedback submitted successfully!')
  } catch (err) {
    console.error('Feedback submission error:', err)
    showError(getErrorMessage(err, 'Unable to submit feedback.'))
  } finally {
    setSubmittingFeedbackId(null)
  }
}

  // LOAD DATA AFTER LOGIN
  useEffect(() => {
    if (!token || !role) return
    const timer = window.setTimeout(() => {
      fetchComplaints()
      if (role === 'ADMIN') { fetchWorkers(); fetchFeedback(); fetchOverdue(); fetchActivity() }
      if (role === 'USER') { fetchFeedback(); fetchProfile(); fetchNotifications() }
      if (role === 'WORKER') { fetchWorkerCitizenFeedback(); fetchWorkerStats(); fetchProfile() }
    }, 0)
    return () => window.clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token, role])

  useEffect(() => {
    if (!token || role !== 'USER') return undefined
    const timer = window.setInterval(async () => {
      try { const response = await API.get('/user/notifications'); setNotifications(Array.isArray(response.data) ? response.data : []) }
      catch (err) { console.warn('Notification refresh failed', err) }
    }, 30000)
    return () => window.clearInterval(timer)
  }, [token, role])

  // ASSIGN WORKER
  const handleAssignWorker = async complaintId => {
    const workerEmail = selectedWorkers[complaintId]

    if (!workerEmail) {
      showError('Please select a worker first.')
      return
    }

    setAssigningId(complaintId)
    setMessage('')
    setError('')

    try {
      await API.put(
        `/admin/complaints/${complaintId}/assign`,
        null,
        {
          params: { workerEmail }
        }
      )

      await fetchComplaints()

      showMessage('Worker assigned successfully!')
    } catch (err) {
      console.error('Assignment error:', err)

      showError(
        getErrorMessage(err, 'Unable to assign worker.')
      )
    } finally {
      setAssigningId(null)
    }
  }

  // CITIZEN SUBMITS COMPLAINT
  const handleInputChange = event => {
    const { name, value } = event.target

    setForm(previous => ({
      ...previous,
      [name]: value
    }))
  }

  const clearFilters = () => {
    setSearch('')
    setStatusFilter('All')
    setTypeFilter('All')
    setPriorityFilter('All')
    setSortBy('date-desc')
    setCurrentPage(1)
  }

  
const handleSubmit = async event => {
  event.preventDefault()

  setSubmitting(true)
  setMessage('')
  setError('')

  try {
    const complaintData = new FormData()

    complaintData.append('customerName', form.customerName)
    complaintData.append('address', form.address)
    complaintData.append('complaintType', form.complaintType)
    complaintData.append('priority', form.priority)
    complaintData.append('description', form.description)

    if (complaintPhoto) {
      complaintData.append('photo', complaintPhoto)
    }

    await API.post('/user/complaints', complaintData)

    setForm({ ...initialForm })
    setComplaintPhoto(null)

    await fetchComplaints()

    showMessage('Complaint submitted successfully!')
    setPage('complaints')
    clearFilters()

  } catch (err) {
    console.error('Submit error:', err)

    showError(
      getErrorMessage(err, 'Unable to submit complaint.')
    )
  } finally {
    setSubmitting(false)
  }
}


  // ADMIN UPDATES STATUS
  const handleStatusUpdate = async (id, status) => {
    if (['Cancelled', 'Rejected'].includes(status) &&
        !window.confirm(`Mark complaint #${id} as ${status.toLowerCase()}? This will close the complaint.`)) return
    setUpdatingId(id)
    setMessage('')
    setError('')

    try {
      await API.put(`/admin/complaints/${id}/status`, null, {
        params: { status }
      })

      setComplaints(previous =>
        previous.map(complaint =>
          complaint.complaintId === id
            ? { ...complaint, status }
            : complaint
        )
      )

      setSelectedComplaint(previous =>
        previous?.complaintId === id
          ? { ...previous, status }
          : previous
      )

      showMessage('Complaint status updated successfully!')
    } catch (err) {
      showError(
        getErrorMessage(err, 'Unable to update complaint status.')
      )
    } finally {
      setUpdatingId(null)
    }
  }

  // ADMIN UPDATES PRIORITY
  const handlePriorityUpdate = async (id, priority) => {
    setUpdatingId(id)
    setMessage('')
    setError('')

    try {
      await API.put(`/admin/complaints/${id}/priority`, null, {
        params: { priority }
      })

      setComplaints(previous =>
        previous.map(complaint =>
          complaint.complaintId === id
            ? { ...complaint, priority }
            : complaint
        )
      )

      setSelectedComplaint(previous =>
        previous?.complaintId === id
          ? { ...previous, priority }
          : previous
      )

      showMessage('Complaint priority updated successfully!')
    } catch (err) {
      showError(
        getErrorMessage(err, 'Unable to update complaint priority.')
      )
    } finally {
      setUpdatingId(null)
    }
  }

  // ADMIN DELETES COMPLAINT
  const handleDelete = async id => {
    const confirmed = window.confirm(
      `Are you sure you want to delete complaint #${id}?`
    )

    if (!confirmed) return

    setDeletingId(id)
    setMessage('')
    setError('')

    try {
      await API.delete(`/admin/complaints/${id}`)

      if (selectedComplaint?.complaintId === id) {
        setSelectedComplaint(null)
      }

      await fetchComplaints()

      showMessage('Complaint deleted successfully!')
    } catch (err) {
      showError(
        getErrorMessage(err, 'Unable to delete complaint.')
      )
    } finally {
      setDeletingId(null)
    }
  }

  // FILTERS AND SEARCH
  const complaintTypes = useMemo(() => {
    return [
      ...new Set(
        complaints
          .map(complaint => complaint.complaintType)
          .filter(Boolean)
      )
    ]
  }, [complaints])

  const filteredComplaints = useMemo(() => {
    const searchText = search.trim().toLowerCase()

    return complaints.filter(complaint => {
      const matchesSearch = [
        complaint.complaintId,
        complaint.customerName,
        complaint.address,
        complaint.complaintType,
        complaint.description,
        complaint.status,
        complaint.priority
      ].some(value =>
        String(value ?? '').toLowerCase().includes(searchText)
      )

      const matchesStatus =
        statusFilter === 'All' ||
        complaint.status === statusFilter

      const matchesType =
        typeFilter === 'All' ||
        complaint.complaintType === typeFilter

      const matchesPriority =
        priorityFilter === 'All' ||
        (complaint.priority || 'Medium') === priorityFilter

      return matchesSearch && matchesStatus && matchesType && matchesPriority
    }).sort((a, b) => {
      if (sortBy === 'priority') {
        const order = { High: 1, Medium: 2, Low: 3 }
        return (order[a.priority || 'Medium'] || 2) - (order[b.priority || 'Medium'] || 2)
      }
      if (sortBy === 'status') return String(a.status).localeCompare(String(b.status))
      const dateA = a.createdAt ? Date.parse(a.createdAt) : Number(a.complaintId)
      const dateB = b.createdAt ? Date.parse(b.createdAt) : Number(b.complaintId)
      if (sortBy === 'date-asc') return dateA - dateB
      return dateB - dateA
    })
  }, [
    complaints,
    search,
    statusFilter,
    typeFilter,
    priorityFilter,
    sortBy
  ])

  const pageCount = Math.max(1, Math.ceil(filteredComplaints.length / pageSize))
  const paginatedComplaints = filteredComplaints.slice((currentPage - 1) * pageSize, currentPage * pageSize)
  const visibleFeedbacks = feedbacks.slice((feedbackPage-1)*feedbackPageSize,feedbackPage*feedbackPageSize)
  const visibleWorkerFeedbacks = workerFeedbacks.slice((workerFeedbackPage-1)*feedbackPageSize,workerFeedbackPage*feedbackPageSize)
  const visibleWorkerCitizenFeedbacks = workerCitizenFeedbacks.slice((workerCitizenFeedbackPage-1)*feedbackPageSize,workerCitizenFeedbackPage*feedbackPageSize)


  // DASHBOARD STATISTICS
  const totalComplaints = complaints.length

  const pendingComplaints = complaints.filter(
    complaint => complaint.status === 'Pending'
  ).length

  const assignedComplaints = complaints.filter(
    complaint => complaint.status === 'Assigned'
  ).length

  const inProgressComplaints = complaints.filter(
    complaint => complaint.status === 'In Progress'
  ).length

  const resolvedComplaints = complaints.filter(
    complaint => complaint.status === 'Resolved'
  ).length
  const cancelledComplaints = complaints.filter(complaint => complaint.status === 'Cancelled').length
  const rejectedComplaints = complaints.filter(complaint => complaint.status === 'Rejected').length

  const highPriorityComplaints = complaints.filter(
    complaint => complaint.priority === 'High'
  ).length

  const mediumPriorityComplaints = complaints.filter(
    complaint => complaint.priority === 'Medium'
  ).length

  const lowPriorityComplaints = complaints.filter(
    complaint => complaint.priority === 'Low'
  ).length

  const complaintTypeStats = useMemo(() => complaints.reduce((stats, complaint) => {
    const type = complaint.complaintType || 'Unspecified'
    stats[type] = (stats[type] || 0) + 1
    return stats
  }, {}), [complaints])
  const averageFeedbackRating = feedbacks.length
    ? (feedbacks.reduce((sum, item) => sum + (Number(item.rating) || 0), 0) / feedbacks.length).toFixed(1)
    : '—'
  const averageWorkerFeedbackRating = workerFeedbacks.length
    ? (workerFeedbacks.reduce((sum, item) => sum + (Number(item.rating) || 0), 0) / workerFeedbacks.length).toFixed(1)
    : '—'

  // CSS HELPERS
  const getPriorityClass = priority => {
    const value = (priority || 'Medium').toLowerCase()
    return `priority-badge priority-${value}`
  }

  const getStatusClass = status => {
    const value = (status || 'Pending')
      .toLowerCase()
      .replace(/\s+/g, '-')

    return `status-badge status-${value}`
  }

  // NAVIGATION
  const navigate = selectedPage => {
    setPage(selectedPage)
    setMessage('')
    setError('')

    if (selectedPage === 'complaints' || selectedPage === 'dashboard') {
      fetchComplaints()
    }

    if (selectedPage === 'feedback') {
      fetchFeedback()
    }

    if (selectedPage === 'createWorker') {
      fetchWorkers()
    }
  }

  // LOGIN AND REGISTRATION SCREEN
  if (!token || !role) {
    return (
      <div className="app auth-page">
        <section className="form-container auth-card">
          <h1>💧 SWCMS</h1>

          <h2>
            {authPage === 'register'
              ? 'Create Citizen Account'
              : 'Welcome Back'}
          </h2>

          <p>Smart Water Complaint Management System</p>

          {message && <div className="message">{message}</div>}
          {error && <div className="message error">{error}</div>}

          {authPage === 'login' && (
            <div className="filter-container">
              <button
                type="button"
                className={loginRole === 'USER' ? 'submit-btn' : 'refresh-btn'}
                onClick={() => setLoginRole('USER')}
              >
                Citizen Login
              </button>

              <button
                type="button"
                className={loginRole === 'WORKER' ? 'submit-btn' : 'refresh-btn'}
                onClick={() => setLoginRole('WORKER')}
              >
                Worker Login
              </button>

              <button
                type="button"
                className={loginRole === 'ADMIN' ? 'submit-btn' : 'refresh-btn'}
                onClick={() => setLoginRole('ADMIN')}
              >
                Admin Login
              </button>
            </div>
          )}

          <form onSubmit={authPage === 'register' ? handleRegister : handleLogin}>
            {authPage === 'register' && (
              <>
                <label>Full Name</label>
                <input
                  name="name"
                  value={authForm.name}
                  onChange={handleAuthInput}
                  placeholder="Enter your name"
                  required
                  maxLength="100"
                />
              </>
            )}

            <label>Email</label>
            <input
              type="email"
              name="email"
              value={authForm.email}
              onChange={handleAuthInput}
              placeholder="Enter your email"
              required
            />

            <label>Password</label>
            <input
              type="password"
              name="password"
              value={authForm.password}
              onChange={handleAuthInput}
              placeholder="Enter your password"
              minLength="8"
              required
            />

            <button
              type="submit"
              className="submit-btn"
              disabled={authLoading}
            >
              {authLoading
                ? 'Please wait...'
                : authPage === 'register'
                  ? 'Register'
                  : 'Login'}
            </button>
          </form>

          {authPage === 'login' && loginRole === 'USER' && (
            <p>
              Don't have an account?{' '}
              <button
                type="button"
                className="view-btn"
                onClick={() => {
                  setAuthPage('register')
                  setMessage('')
                  setError('')
                }}
              >
                Register
              </button>
            </p>
          )}

          {authPage === 'register' && (
            <button
              type="button"
              className="refresh-btn"
              onClick={() => {
                setAuthPage('login')
                setMessage('')
                setError('')
              }}
            >
              Back to Login
            </button>
          )}
        </section>
      </div>
    )
  }

  // MAIN APPLICATION
  return (
    <div className="app">

      {/* SIDEBAR */}
      <aside className="sidebar">
        <h2>💧 SWCMS</h2>

        <nav>
          <button
            className={page === 'dashboard' ? 'active' : ''}
            onClick={() => navigate('dashboard')}
          >
            🏠 Dashboard
          </button>

          <button
            className={page === 'complaints' ? 'active' : ''}
            onClick={() => navigate('complaints')}
          >
            📋 {isAdmin ? 'All Complaints' : isWorker ? 'Assigned Complaints' : 'My Complaints'}
          
          </button>
          {isWorker && (
  <button
    className={page === 'worker-feedback' ? 'active' : ''}
    onClick={() => navigate('worker-feedback')}
  >
    ⭐ Citizen Feedback
  </button>
)}

          {isCitizen && (
            <button
              className={page === 'submit' ? 'active' : ''}
              onClick={() => navigate('submit')}
            >
              ➕ Submit Complaint
            </button>
          )}
          {isCitizen && (
            <button className={page === 'notifications' ? 'active' : ''} onClick={() => { navigate('notifications'); fetchNotifications() }}>
              🔔 Notifications {notifications.length > 0 && <span className="nav-count">{notifications.length}</span>}
            </button>
          )}

          {isCitizen && (
            <button
              className={page === 'feedback' ? 'active' : ''}
              onClick={() => navigate('feedback')}
            >
              ⭐ My Feedback
            </button>
          )}

          {isAdmin && (
            <>
              <button className={page === 'workers' ? 'active' : ''} onClick={() => { navigate('workers'); fetchWorkers() }}>👷 Worker Management</button>
              <button
                className={page === 'createAdmin' ? 'active' : ''}
                onClick={() => navigate('createAdmin')}
              >
                ➕ Create Admin
              </button>

              <button
                className={page === 'createWorker' ? 'active' : ''}
                onClick={() => navigate('createWorker')}
              >
                👷 Create Worker
              </button>

              <button
                className={page === 'feedback' ? 'active' : ''}
                onClick={() => navigate('feedback')}
              >
                ⭐ Citizen Feedback
              </button>
            </>
          )}

          {!isAdmin && <button className={page === 'profile' ? 'active' : ''} onClick={() => { navigate('profile'); fetchProfile() }}>👤 My Profile</button>}

          <button onClick={logout}>
            🚪 Logout
          </button>
        </nav>
      </aside>

      {/* MAIN CONTENT */}
      <main className="main-content">

        <header>
          <div>
            <h1>
              {page === 'dashboard' && 'Dashboard'}

              {page === 'complaints' && (
                isAdmin
                  ? 'All Complaints'
                  : isWorker
                    ? 'Active Workload'
                    : 'My Complaints'
              )}

              {page === 'submit' && 'Submit Complaint'}
              {page === 'createAdmin' && 'Create Admin Account'}
              {page === 'createWorker' && 'Create Worker Account'}

              {page === 'feedback' && (
                isAdmin ? 'Feedback Management' : 'My Feedback'
              )}
              {page === 'worker-feedback' && 'Citizen Feedback'}
              {page === 'workers' && 'Worker Management'}
              {page === 'profile' && 'My Profile'}
              {page === 'notifications' && 'Notifications'}
            </h1>

            <p>Smart Water Complaint Management System</p>
          </div>

          <span>
            {profile?.name || (isAdmin ? 'Admin' : isWorker ? 'Worker' : 'Citizen')}
          </span>
        </header>

        {message && <div className="message">{message}</div>}
        {error && <div className="message error">{error}</div>}

        {/* DASHBOARD */}
        {page === 'dashboard' && (
          <>
            <section className="stats">
              <div className="stat-card">
                <h3>
                  {isAdmin
                    ? 'Total Complaints'
                    : isWorker
                      ? 'Assigned Complaints'
                      : 'My Complaints'}
                </h3>
                <h2>{isWorker ? (workerStats?.active ?? totalComplaints) : totalComplaints}</h2>
              </div>

              <div className="stat-card">
                <h3>Pending</h3>
                <h2>{pendingComplaints}</h2>
              </div>

              <div className="stat-card">
                <h3>Assigned</h3>
                <h2>{assignedComplaints}</h2>
              </div>

              <div className="stat-card">
                <h3>In Progress</h3>
                <h2>{inProgressComplaints}</h2>
              </div>

              <div className="stat-card">
                <h3>Resolved</h3>
                <h2>{resolvedComplaints}</h2>
              </div>
                {(isAdmin || isCitizen) && <div className="stat-card"><h3>Cancelled</h3><h2>{cancelledComplaints}</h2></div>}
                {isAdmin && <div className="stat-card"><h3>Rejected</h3><h2>{rejectedComplaints}</h2></div>}
            </section>

            <section className="welcome">
              <h2>
                {isAdmin
                  ? 'Welcome, Administrator!'
                  : isWorker
                    ? 'Welcome, Worker!'
                    : 'Welcome to SWCMS!'}
              </h2>

              <p>
                {isAdmin
                  ? 'Review complaints, manage priorities and assign workers.'
                  : isWorker
                    ? 'View your assigned complaints and update their progress.'
                    : 'Submit water-related complaints and track their progress.'}
              </p>

              {isCitizen && (
                <button
                  className="submit-btn"
                  onClick={() => navigate('submit')}
                >
                  + Submit New Complaint
                </button>
              )}
            </section>

            {isAdmin && (
              <section className="stats">
                <div className="stat-card">
                  <h3>High Priority</h3>
                  <h2>{highPriorityComplaints}</h2>
                </div>

                <div className="stat-card">
                  <h3>Medium Priority</h3>
                  <h2>{mediumPriorityComplaints}</h2>
                </div>

                <div className="stat-card">
                  <h3>Low Priority</h3>
                  <h2>{lowPriorityComplaints}</h2>
                </div>

                <div className="stat-card">
                  <h3>Available Workers</h3>
                  <h2>{workers.length}</h2>
                </div>
                <div className="stat-card">
                  <h3>Citizen Feedback Avg.</h3>
                  <h2>{averageFeedbackRating}{feedbacks.length ? '/5' : ''}</h2>
                </div>
                <div className="stat-card">
                  <h3>Worker Feedback Avg.</h3>
                  <h2>{averageWorkerFeedbackRating}{workerFeedbacks.length ? '/5' : ''}</h2>
                </div>
              </section>
            )}

            {isAdmin && Object.keys(complaintTypeStats).length > 0 && (
              <section className="analytics-card">
                <div className="section-heading"><div><span className="eyebrow">ANALYTICS</span><h2>Complaints by type</h2></div></div>
                <div className="bar-chart">{Object.entries(complaintTypeStats).map(([type, count]) => (
                  <div className="bar-row" key={type}><span>{type}</span><div className="bar-track"><i style={{ width: `${Math.max(4, count / Math.max(...Object.values(complaintTypeStats)) * 100)}%` }} /></div><b>{count}</b></div>
                ))}</div>
              </section>
            )}

            {isAdmin && <section className="admin-tools panel">
              <div><span className="eyebrow">SERVICE LEVEL</span><h2>Overdue cases</h2><p>Complaints open for more than seven days.</p></div>
              <button className="submit-btn" onClick={escalateOverdue}>Escalate {overdueIds.length} overdue complaints</button>
            </section>}

            {isWorker && workerStats && <>
              <section className="stats">
                <div className="stat-card"><h3>Active Workload</h3><h2>{workerStats.active}</h2></div>
                <div className="stat-card"><h3>Completed Work</h3><h2>{workerStats.resolved}</h2></div>
                <div className="stat-card"><h3>Citizen Rating</h3><h2>{Number(workerStats.citizenFeedbackAverage).toFixed(1)}/5</h2></div>
              </section>
                  <section className="analytics-card"><h2>Completed work history</h2>{workerStats.completedWork?.length ? <div className="table-scroll"><table className="complaints-table"><thead><tr><th>Complaint</th><th>Type</th><th>Priority</th><th>Completed</th></tr></thead><tbody>{workerStats.completedWork.map(work => <tr key={work.complaintId}><td>#{work.complaintId}</td><td>{work.complaintType}</td><td>{work.priority}</td><td>{work.resolvedAt ? new Date(work.resolvedAt).toLocaleDateString() : 'Resolved'}</td></tr>)}</tbody></table></div> : <p>No completed work yet.</p>}</section>
            </>}

            <section className="table-container">
              <div className="table-header">
                <h2>Recent Complaints</h2>

                <button
                  className="refresh-btn"
                  onClick={fetchComplaints}
                  disabled={loading}
                >
                  {loading ? 'Loading...' : '↻ Refresh'}
                </button>
              </div>

              {loading ? (
                <p>Loading complaints...</p>
              ) : complaints.length === 0 ? (
                <p>No complaints available.</p>
              ) : (
                <div className="table-scroll">
                  <table className="complaints-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Customer</th>
                        <th>Type</th>
                        <th>Priority</th>
                        <th>Status</th>
                        <th>Action</th>
                      </tr>
                    </thead>

                    <tbody>
                      {[...complaints]
                        .sort((a, b) =>
                          Number(b.complaintId) - Number(a.complaintId)
                        )
                        .slice(0, 5)
                        .map(complaint => (
                          <tr key={complaint.complaintId}>
                            <td>{complaint.complaintId}</td>
                            <td>{complaint.customerName}</td>
                            <td>{complaint.complaintType}</td>

                            <td>
                              <span className={getPriorityClass(complaint.priority)}>
                                {complaint.priority || 'Medium'}
                              </span>
                            </td>

                            <td>
                              <span className={getStatusClass(complaint.status)}>
                                {complaint.status || 'Pending'}
                              </span>
                            </td>

                            <td>
                              <button
                                className="view-btn"
                                onClick={() => {
  setSelectedComplaint(complaint)
  fetchComplaintHistory(complaint.complaintId)
}}
                              >
                                View
                              </button>
                            </td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              )}

              <button
                className="refresh-btn"
                onClick={() => navigate('complaints')}
              >
                View All Complaints
              </button>
            </section>
            {isAdmin && <section className="panel table-container"><div className="table-header"><div><span className="eyebrow">ACTIVITY</span><h2>Recent status updates</h2></div><button className="refresh-btn" onClick={fetchActivity}>Refresh</button></div>
              {activity.length ? <div className="notification-list">{activity.slice(0,8).map(item => <div className="notification-item" key={item.historyId}><span className="notification-dot"/><span><strong>Complaint #{item.complaintId}: {item.oldStatus} → {item.newStatus}</strong><small>{item.remarks || 'Status updated'} · {item.updatedAt ? new Date(item.updatedAt).toLocaleString() : ''}</small></span></div>)}</div> : <p>No recent activity.</p>}
            </section>}
            {isAdmin && <section className="panel table-container"><div className="table-header"><div><span className="eyebrow">TEAM PERFORMANCE</span><h2>Worker workload</h2></div><button className="refresh-btn" onClick={() => { navigate('workers'); fetchWorkers() }}>Manage workers</button></div>
              {workers.length ? <div className="table-scroll"><table className="complaints-table"><thead><tr><th>Worker</th><th>Account</th><th>Active</th><th>Resolved</th><th>Citizen rating</th></tr></thead><tbody>{[...workers].sort((a,b) => (b.resolvedCount||0)-(a.resolvedCount||0)).slice(0,8).map(worker => <tr key={worker.userId}><td>{worker.name}</td><td><span className={`account-state ${worker.active?'enabled':'disabled'}`}>{worker.active?'Active':'Inactive'}</span></td><td>{worker.assignedCount ?? 0}</td><td>{worker.resolvedCount ?? 0}</td><td>{worker.averageRating ? `${Number(worker.averageRating).toFixed(1)}/5` : '—'}</td></tr>)}</tbody></table></div> : <p>No workers available.</p>}
            </section>}
          </>
        )}

        {/* COMPLAINTS PAGE */}
        {page === 'complaints' && (
          <section className="table-container">
            <div className="table-header">
              <h2>
                {isAdmin
                  ? 'Complaint Records'
                  : isWorker
                    ? 'My Assigned Work'
                    : 'My Complaint Records'}
              </h2>

              <button
                className="refresh-btn"
                onClick={fetchComplaints}
                disabled={loading}
              >
                {loading ? 'Loading...' : '↻ Refresh'}
              </button>
            </div>

            {/* FILTERS */}
            <div className="filter-container">
              <input
                className="search-input"
                type="text"
                placeholder="Search complaints..."
                value={search}
                onChange={event => { setSearch(event.target.value); setCurrentPage(1) }}
              />

              <select
                className="filter-select"
                value={statusFilter}
                onChange={event => { setStatusFilter(event.target.value); setCurrentPage(1) }}
              >
                <option value="All">All Statuses</option>

                {statusOptions.map(status => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>

              <select
                className="filter-select"
                value={typeFilter}
                onChange={event => { setTypeFilter(event.target.value); setCurrentPage(1) }}
              >
                <option value="All">All Complaint Types</option>

                {complaintTypes.map(type => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>

              <select
                className="filter-select"
                value={priorityFilter}
                onChange={event => { setPriorityFilter(event.target.value); setCurrentPage(1) }}
              >
                <option value="All">All Priorities</option>

                {priorityOptions.map(priority => (
                  <option key={priority} value={priority}>
                    {priority}
                  </option>
                ))}
              </select>

              <select className="filter-select" value={sortBy} onChange={event => { setSortBy(event.target.value); setCurrentPage(1) }} aria-label="Sort complaints">
                <option value="date-desc">Newest first</option><option value="date-asc">Oldest first</option><option value="priority">Priority</option><option value="status">Status</option>
              </select>
              <select className="filter-select page-size" value={pageSize} onChange={event => { setPageSize(Number(event.target.value)); setCurrentPage(1) }} aria-label="Rows per page">
                <option value={10}>10 per page</option><option value={25}>25 per page</option><option value={50}>50 per page</option>
              </select>

              <button
                className="clear-filter-btn"
                onClick={clearFilters}
              >
                Clear Filters
              </button>
            </div>

            <p className="result-count">
              Showing {filteredComplaints.length} of {totalComplaints} complaints
            </p>

            {loading ? (
              <p>Loading complaints...</p>
            ) : filteredComplaints.length === 0 ? (
              <p>No matching complaints found.</p>
            ) : (
              <div className="table-scroll">
                <table className="complaints-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Customer</th>
                      <th>Address</th>
                      <th>Type</th>
                      <th>Description</th>
                      <th>Priority</th>
                      <th>Status</th>

                      {isAdmin && <th>Assign Worker</th>}
                      {isAdmin && <th>Actions</th>}
                      {isWorker && <th>Update Progress</th>}
                    </tr>
                  </thead>

                  <tbody>
                    {paginatedComplaints.map(complaint => (
                      <tr key={complaint.complaintId}>
                        <td>{complaint.complaintId}</td>
                        <td>{complaint.customerName}</td>
                        <td>{complaint.address}</td>
                        <td>{complaint.complaintType}</td>
                        <td>{complaint.description}</td>

                        {/* PRIORITY */}
                        <td>
                          {isAdmin ? (
                            <select
                              value={complaint.priority || 'Medium'}
                              disabled={updatingId === complaint.complaintId}
                              onChange={event =>
                                handlePriorityUpdate(
                                  complaint.complaintId,
                                  event.target.value
                                )
                              }
                            >
                              {priorityOptions.map(priority => (
                                <option key={priority} value={priority}>
                                  {priority}
                                </option>
                              ))}
                            </select>
                          ) : (
                            <span className={getPriorityClass(complaint.priority)}>
                              {complaint.priority || 'Medium'}
                            </span>
                          )}
                        </td>

                        {/* STATUS */}
                        <td>
                          {isAdmin ? <select value={complaint.status || 'Pending'} disabled={updatingId === complaint.complaintId || ['Resolved','Cancelled','Rejected'].includes(complaint.status)} onChange={event => handleStatusUpdate(complaint.complaintId,event.target.value)}>
                            {( {Pending:['Pending','Cancelled','Rejected'],Assigned:['Assigned','In Progress','Cancelled','Rejected'],'In Progress':['In Progress','Assigned','Resolved','Cancelled','Rejected']}[complaint.status] || [complaint.status || 'Pending']).map(status => <option key={status} value={status}>{status}</option>)}
                          </select> : <span className={getStatusClass(complaint.status)}>{complaint.status || 'Pending'}</span>}
                          {overdueIds.includes(complaint.complaintId) && <span className="overdue-pill">Overdue</span>}
                        </td>

                        {/* ADMIN WORKER ASSIGNMENT */}
                        {isAdmin && (
                          <td>
                            <select
                              value={selectedWorkers[complaint.complaintId] || ''}
                              onChange={event =>
                                setSelectedWorkers(previous => ({
                                  ...previous,
                                  [complaint.complaintId]: event.target.value
                                }))
                              }
                            >
                              <option value="">Select Worker</option>

                              {workers.filter(worker => worker.active !== false).map(worker => (
                                <option
                                  key={worker.userId}
                                  value={worker.email}
                                >
                                  {worker.name} ({worker.email})
                                </option>
                              ))}
                            </select>

                            <button
                              className="submit-btn"
                              disabled={
                                assigningId === complaint.complaintId ||
                                workers.filter(worker => worker.active !== false).length === 0 ||
                                ['Resolved','Cancelled','Rejected'].includes(complaint.status)
                              }
                              onClick={() =>
                                handleAssignWorker(complaint.complaintId)
                              }
                            >
                              {assigningId === complaint.complaintId
                                ? 'Assigning...'
                                : 'Assign / Reassign'}
                            </button>
                          </td>
                        )}

                        {/* ADMIN ACTIONS */}
                        {isAdmin && (
                          <td className="action-buttons">
                            <button
                              className="view-btn"
                              onClick={() => {
  setSelectedComplaint(complaint)
  fetchComplaintHistory(complaint.complaintId)
}}
                            >
                              View
                            </button>

                            <button
                              className="delete-btn"
                              disabled={deletingId === complaint.complaintId}
                              onClick={() => handleDelete(complaint.complaintId)}
                            >
                              {deletingId === complaint.complaintId
                                ? 'Deleting...'
                                : 'Delete'}
                            </button>
                          </td>
                        )}

                        {/* WORKER STATUS UPDATE */}
                        {/* WORKER COMPLETION FORM */}
{isWorker && (
  <td>
    {complaint.status === 'Resolved' ? (
      <WorkerFeedbackForm
        complaint={complaint}
        onSuccess={async () => { await fetchComplaints(); await fetchWorkerStats() }}
      />
    ) : ['Cancelled','Rejected'].includes(complaint.status) ? (
      <span className={getStatusClass(complaint.status)}>{complaint.status}</span>
    ) : (
      <WorkerCompletionForm
        complaint={complaint}
        onSuccess={async () => { await fetchComplaints(); await fetchWorkerStats(); showMessage('Work status updated successfully.') }}
      />
    )}
  </td>
)}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="pagination-bar"><span>Page {currentPage} of {pageCount}</span><div><button className="refresh-btn" disabled={currentPage <= 1} onClick={() => setCurrentPage(page => page - 1)}>Previous</button><button className="refresh-btn" disabled={currentPage >= pageCount} onClick={() => setCurrentPage(page => page + 1)}>Next</button></div></div>
          </section>
        )}

        {/* SUBMIT COMPLAINT */}
        {page === 'submit' && isCitizen && (
          <section className="form-container">
            <h2>Register a New Complaint</h2>

            <form onSubmit={handleSubmit}>
              <label>Customer Name</label>
              <input
                type="text"
                name="customerName"
                value={form.customerName}
                onChange={handleInputChange}
                placeholder="Enter customer name"
                maxLength="100"
                required
              />

              <label>Address</label>
              <input
                type="text"
                name="address"
                value={form.address}
                onChange={handleInputChange}
                placeholder="Enter address"
                required
              />

              <label>Complaint Type</label>
              <select
                name="complaintType"
                value={form.complaintType}
                onChange={handleInputChange}
                required
              >
                <option value="">Select complaint type</option>
                <option value="Low Water Pressure">Low Water Pressure</option>
                <option value="Water Leakage">Water Leakage</option>
                <option value="Water Quality">Water Quality</option>
                <option value="No Water Supply">No Water Supply</option>
                <option value="Other">Other</option>
              </select>

              <label>Priority</label>
              <select
                name="priority"
                value={form.priority}
                onChange={handleInputChange}
                required
              >
                {priorityOptions.map(priority => (
                  <option key={priority} value={priority}>
                    {priority}
                  </option>
                ))}
              </select>

              <label>Description</label>
              <textarea
                name="description"
                value={form.description}
                onChange={handleInputChange}
                placeholder="Describe your complaint"
                maxLength="1000"
                required
              />
              <label>Attach Complaint Photo (Optional)</label>

<input
  type="file"
  accept="image/jpeg,image/png,image/webp"
  onChange={(e) => {
    setComplaintPhoto(e.target.files[0] || null);
  }}
/>

{complaintPhoto && (
  <div className="photo-preview">
    <p>Selected: {complaintPhoto.name}</p>

    <img
      src={URL.createObjectURL(complaintPhoto)}
      alt="Complaint preview"
      style={{
        width: "150px",
        height: "120px",
        objectFit: "cover",
        borderRadius: "8px"
      }}
    />

    <button
      type="button"
      onClick={() => setComplaintPhoto(null)}
    >
      Remove Photo
    </button>
  </div>
)}

              <button
                type="submit"
                className="submit-btn"
                disabled={submitting}
              >
                {submitting ? 'Submitting...' : 'Submit Complaint'}
              </button>
            </form>
          </section>
        )}
        {/* WORKER CITIZEN FEEDBACK PAGE */}
{page === 'worker-feedback' && isWorker && (
  <section className="table-container">
    <div className="table-header">
      <h2>Feedback from Citizens</h2>

      <div className="table-actions"><select className="filter-select" value={feedbackPageSize} onChange={event=>{setFeedbackPageSize(Number(event.target.value));setWorkerCitizenFeedbackPage(1)}}><option value={10}>10 per page</option><option value={25}>25 per page</option></select><button className="refresh-btn" onClick={fetchWorkerCitizenFeedback}>↻ Refresh</button></div>
    </div>

    {workerCitizenFeedbacks.length === 0 ? (
      <p>No citizen feedback available yet.</p>
    ) : (
      visibleWorkerCitizenFeedbacks.map(feedback => (
        <div className="feedback-card" key={feedback.feedbackId}>
          <h3>Complaint #{feedback.complaintId}</h3>

          <p>
            <strong>Rating:</strong> {'⭐'.repeat(feedback.rating || 0)}
          </p>

          <p>
            <strong>Comment:</strong> {feedback.comment || 'No comment'}
          </p>
          {feedback.photoPath && (
  <FeedbackPhoto feedbackId={feedback.feedbackId} />
)}

          <p>
            <strong>Submitted:</strong>{' '}
            {feedback.submittedAt
              ? new Date(feedback.submittedAt).toLocaleString()
              : 'N/A'}
          </p>
        </div>
      ))
    )}
    <Pagination page={workerCitizenFeedbackPage} total={workerCitizenFeedbacks.length} pageSize={feedbackPageSize} onChange={setWorkerCitizenFeedbackPage} />
  </section>
)}

        {page === 'workers' && isAdmin && (
          <section className="panel table-container">
            <div className="table-header"><div><span className="eyebrow">TEAM</span><h2>Worker management</h2></div><button className="refresh-btn" onClick={fetchWorkers}>Refresh</button></div>
            {workers.length === 0 ? <div className="empty-state"><h3>No workers yet</h3><p>Create a worker account to begin assigning complaints.</p></div> : <div className="table-scroll"><table className="complaints-table"><thead><tr><th>Name</th><th>Email</th><th>Account</th><th>Active Work</th><th>Resolved</th><th>Rating</th><th>Actions</th></tr></thead><tbody>
              {workers.map(worker => <tr key={worker.userId}>
                <td>{workerEdit?.userId === worker.userId ? <input value={workerEdit.name} onChange={e => setWorkerEdit({...workerEdit,name:e.target.value})} /> : worker.name}</td>
                <td>{workerEdit?.userId === worker.userId ? <input type="email" value={workerEdit.email} onChange={e => setWorkerEdit({...workerEdit,email:e.target.value})} /> : worker.email}</td>
                <td><span className={`account-state ${worker.active ? 'enabled' : 'disabled'}`}>{worker.active ? 'Active' : 'Inactive'}</span></td>
                <td>{worker.assignedCount ?? 0}</td><td>{worker.resolvedCount ?? 0}</td><td>{worker.averageRating ? `${Number(worker.averageRating).toFixed(1)}/5` : '—'}</td>
                <td className="action-buttons">{workerEdit?.userId === worker.userId ? <><button className="view-btn" onClick={saveWorker}>Save</button><button className="clear-filter-btn" onClick={() => setWorkerEdit(null)}>Cancel</button></> : <><button className="view-btn" onClick={() => setWorkerEdit({...worker})}>Edit</button><button className="clear-filter-btn" onClick={async () => { if (!window.confirm(`${worker.active ? 'Deactivate' : 'Activate'} ${worker.name}?`)) return; try { const updated = await API.put(`/admin/workers/${worker.userId}`, {...worker,active:!worker.active}); setWorkers(list => list.map(row => row.userId === updated.data.userId ? updated.data : row)); showMessage(`Worker ${updated.data.active ? 'activated' : 'deactivated'}.`) } catch (err) { showError(getErrorMessage(err,'Unable to change worker status.')) } }}>{worker.active ? 'Deactivate' : 'Activate'}</button><button className="refresh-btn" onClick={async () => { if (selectedWorkerId === worker.userId) { setSelectedWorkerId(null); return }; try { const response = await API.get(`/admin/workers/${worker.userId}/complaints`); setSelectedWorkerId(worker.userId); setSelectedWorkerComplaints(response.data || []) } catch (err) { showError(getErrorMessage(err,'Unable to load worker complaints.')) } }}>Work</button></>}</td>
              </tr>)}
            </tbody></table></div>}
            {selectedWorkerId && <div className="worker-work-panel"><h3>Worker #{selectedWorkerId} assigned complaints</h3>{selectedWorkerComplaints.length ? selectedWorkerComplaints.map(item => <div className="worker-work-row" key={item.complaintId}><strong>#{item.complaintId} · {item.complaintType}</strong><span>{item.customerName}</span><span>{item.status}</span><span>{item.priority} priority</span></div>) : <p>No complaints assigned.</p>}</div>}
          </section>
        )}

        {page === 'profile' && !isAdmin && (
          <div className="profile-grid">
            <section className="form-container panel"><span className="eyebrow">ACCOUNT</span><h2>Profile details</h2>
              <form onSubmit={saveProfile}><label>Full name</label><input value={profileForm.name} onChange={e => setProfileForm({...profileForm,name:e.target.value})} required maxLength="100" />
                <label>Email address</label><input type="email" value={profileForm.email} onChange={e => setProfileForm({...profileForm,email:e.target.value})} required />
                <button className="submit-btn" type="submit">Save profile</button></form>
            </section>
            <section className="form-container panel"><span className="eyebrow">SECURITY</span><h2>Change password</h2>
              <form onSubmit={changePassword}><label>Current password</label><input type="password" value={passwordForm.currentPassword} onChange={e => setPasswordForm({...passwordForm,currentPassword:e.target.value})} required />
                <label>New password</label><input type="password" minLength="8" value={passwordForm.newPassword} onChange={e => setPasswordForm({...passwordForm,newPassword:e.target.value})} required />
                <button className="submit-btn" type="submit">Update password</button></form>
            </section>
          </div>
        )}

        {page === 'notifications' && isCitizen && <section className="panel table-container"><div className="table-header"><div><span className="eyebrow">UPDATES</span><h2>Complaint status activity</h2></div><button className="refresh-btn" onClick={fetchNotifications}>Refresh</button></div>
          {notifications.length === 0 ? <div className="empty-state"><h3>You're all caught up</h3><p>Updates to your complaints will appear here.</p></div> : <div className="notification-list">{notifications.map(item => <button className="notification-item" key={item.historyId} onClick={() => { const complaint = complaints.find(c => c.complaintId === item.complaintId); if (complaint) { setSelectedComplaint(complaint); fetchComplaintHistory(item.complaintId) } else navigate('complaints') }}><span className="notification-dot"/><span><strong>Complaint #{item.complaintId}: {item.newStatus}</strong><small>{item.remarks || 'Status updated'} · {item.updatedAt ? new Date(item.updatedAt).toLocaleString() : ''}</small></span></button>)}</div>}
        </section>}

        {/* CREATE ADMIN */}
        {page === 'createAdmin' && isAdmin && (
          <section className="form-container">
            <h2>Create Another Admin</h2>
            <p>Only an existing Admin can create another Admin account.</p>

            <form onSubmit={handleCreateAdmin}>
              <label>Full Name</label>
              <input
                type="text"
                name="name"
                value={adminForm.name}
                onChange={event =>
                  setAdminForm(previous => ({
                    ...previous,
                    name: event.target.value
                  }))
                }
                placeholder="Enter Admin name"
                maxLength="100"
                required
              />

              <label>Email</label>
              <input
                type="email"
                name="email"
                value={adminForm.email}
                onChange={event =>
                  setAdminForm(previous => ({
                    ...previous,
                    email: event.target.value
                  }))
                }
                placeholder="Enter Admin email"
                required
              />

              <label>Password</label>
              <input
                type="password"
                name="password"
                value={adminForm.password}
                onChange={event =>
                  setAdminForm(previous => ({
                    ...previous,
                    password: event.target.value
                  }))
                }
                placeholder="Create a password"
                minLength="8"
                required
              />

              <button
                type="submit"
                className="submit-btn"
                disabled={creatingAdmin}
              >
                {creatingAdmin ? 'Creating Admin...' : 'Create Admin Account'}
              </button>
            </form>
          </section>
        )}

        {/* CREATE WORKER */}
        {page === 'createWorker' && isAdmin && (
          <section className="form-container">
            <h2>Create Worker Account</h2>
            <p>Only an existing Admin can create a Worker account.</p>

            <form onSubmit={handleCreateWorker}>
              <label>Full Name</label>
              <input
                type="text"
                name="name"
                value={workerForm.name}
                onChange={event =>
                  setWorkerForm(previous => ({
                    ...previous,
                    name: event.target.value
                  }))
                }
                placeholder="Enter Worker name"
                maxLength="100"
                required
              />

              <label>Email</label>
              <input
                type="email"
                name="email"
                value={workerForm.email}
                onChange={event =>
                  setWorkerForm(previous => ({
                    ...previous,
                    email: event.target.value
                  }))
                }
                placeholder="Enter Worker email"
                required
              />

              <label>Password</label>
              <input
                type="password"
                name="password"
                value={workerForm.password}
                onChange={event =>
                  setWorkerForm(previous => ({
                    ...previous,
                    password: event.target.value
                  }))
                }
                placeholder="Create a password"
                minLength="8"
                required
              />

              <button
                type="submit"
                className="submit-btn"
                disabled={creatingWorker}
              >
                {creatingWorker ? 'Creating Worker...' : 'Create Worker Account'}
              </button>
            </form>
          </section>
        )}

        {/* FEEDBACK PAGE */}
        {page === 'feedback' && (isAdmin || isCitizen) && (
          <section className="table-container">
            <div className="table-header">
              <h2>
                {isAdmin ? 'All Citizen Feedback' : 'My Feedback'}
              </h2>

              <button
                className="refresh-btn"
                onClick={fetchFeedback}
                disabled={feedbackLoading}
              >
                {feedbackLoading ? 'Loading...' : '↻ Refresh'}
              </button>
              <select className="filter-select" value={feedbackPageSize} onChange={event=>{setFeedbackPageSize(Number(event.target.value));setFeedbackPage(1);setWorkerFeedbackPage(1)}}><option value={10}>10 per page</option><option value={25}>25 per page</option></select>
            </div>

            {/* ADMIN FEEDBACK TABLE */}
            {isAdmin && (
              <>
                {feedbackLoading ? (
                  <p>Loading feedback...</p>
                ) : feedbacks.length === 0 ? (
                  <p>No feedback has been submitted yet.</p>
                ) : (
                  <div className="table-scroll">
                    <table className="complaints-table">
                      <thead>
                        <tr>
                          <th>Feedback ID</th>
                          <th>Complaint ID</th>
                          <th>Citizen</th>
                          <th>Email</th>
                          <th>Rating</th>
                          <th>Comment</th>
                          <th>Photo</th>
                          <th>Submitted At</th>
                        </tr>
                      </thead>

                      <tbody>
                        {visibleFeedbacks.map(feedback => (
                          <tr key={feedback.feedbackId}>
                            <td>{feedback.feedbackId}</td>
                            <td>{feedback.complaintId}</td>
                            <td>{feedback.customerName}</td>
                            <td>{feedback.citizenEmail}</td>
                            <td>
                              {'⭐'.repeat(Number(feedback.rating) || 0)}
                              {' '}({feedback.rating}/5)
                            </td>
                            <td>{feedback.comment || 'No comment'}</td>
                            <td>
  {feedback.photoPath ? (
    <FeedbackPhoto feedbackId={feedback.feedbackId} />
  ) : (
    'No photo'
  )}
</td>
                            <td>
                              {feedback.submittedAt
                                ? new Date(feedback.submittedAt).toLocaleString()
                                : '-'}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
                <Pagination page={feedbackPage} total={feedbacks.length} pageSize={feedbackPageSize} onChange={setFeedbackPage} />
              </>
            )}

            {isAdmin && (
              <section className="table-container">
                <h3>Worker Feedback</h3>
                {workerFeedbacks.length === 0 ? <p>No worker feedback has been submitted yet.</p> : (
                  <div className="table-scroll"><table className="complaints-table">
                    <thead><tr><th>Complaint</th><th>Worker</th><th>Rating</th><th>Comment</th><th>Photo</th><th>Submitted</th></tr></thead>
                    <tbody>{visibleWorkerFeedbacks.map(item => <tr key={item.feedbackId}>
                      <td>#{item.complaintId}</td><td>{item.workerName}</td><td>{item.rating}/5</td><td>{item.comment || 'No comment'}</td>
                      <td>{item.photoPath ? <FeedbackPhoto feedbackId={item.feedbackId} endpoint={`/worker-feedback/${item.feedbackId}/photo`} /> : 'No photo'}</td>
                      <td>{item.submittedAt ? new Date(item.submittedAt).toLocaleString() : '-'}</td>
                    </tr>)}</tbody>
                  </table></div>
                )}
                <Pagination page={workerFeedbackPage} total={workerFeedbacks.length} pageSize={feedbackPageSize} onChange={setWorkerFeedbackPage} />
              </section>
            )}

            
{/* CITIZEN FEEDBACK */}
{isCitizen && (
  <>
    <h3>Submit Feedback</h3>

    <p>
      You can submit feedback after your complaint has been resolved.
      Only one feedback submission is allowed per complaint.
    </p>

    {loading ? (
      <p>Loading complaints...</p>
    ) : (
      <>
        {complaints.filter(
          complaint => complaint.status === 'Resolved'
        ).length === 0 ? (
          <p>You don't have any resolved complaints yet.</p>
        ) : (
          complaints
            .filter(complaint => complaint.status === 'Resolved')
            .map(complaint => {
              const alreadySubmitted = feedbacks.some(
                feedback =>
                  Number(feedback.complaintId) ===
                  Number(complaint.complaintId)
              )

              const draft = feedbackDrafts[complaint.complaintId] || {
                rating: 5,
                comment: ''
              }

              return (
                <div
                  className="welcome"
                  key={complaint.complaintId}
                >
                  <h3>Complaint #{complaint.complaintId}</h3>

                  <p>
                    <strong>Complaint Type:</strong>{' '}
                    {complaint.complaintType}
                  </p>

                  <p>
                    <strong>Status:</strong> {complaint.status}
                  </p>

                  {alreadySubmitted ? (
                    <p><strong>Feedback already submitted.</strong></p>
                  ) : (
                    <form
                      onSubmit={event =>
                        handleFeedbackSubmit(
                          event,
                          complaint.complaintId
                        )
                      }
                    >
                      <label>Rating</label>

                      <select
                        value={draft.rating}
                        onChange={event =>
                          setFeedbackDrafts(previous => ({
                            ...previous,
                            [complaint.complaintId]: {
                              ...draft,
                              rating: Number(event.target.value)
                            }
                          }))
                        }
                      >
                        <option value={5}>5 - Excellent</option>
                        <option value={4}>4 - Very Good</option>
                        <option value={3}>3 - Good</option>
                        <option value={2}>2 - Fair</option>
                        <option value={1}>1 - Poor</option>
                      </select>

                      <label>Comment</label>

                      <textarea
                        value={draft.comment}
                        onChange={event =>
                          setFeedbackDrafts(previous => ({
                            ...previous,
                            [complaint.complaintId]: {
                              ...draft,
                              comment: event.target.value
                            }
                          }))
                        }
                        placeholder="Share your experience..."
                        maxLength="1000"
                      />

                      <label>Attach Feedback Photo (Optional)</label>

                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        onChange={event =>
                          setFeedbackPhotos(previous => ({
                            ...previous,
                            [complaint.complaintId]:
                              event.target.files?.[0] || null
                          }))
                        }
                      />

                      {feedbackPhotos[complaint.complaintId] && (
                        <p>
                          Selected: {
                            feedbackPhotos[complaint.complaintId].name
                          }
                        </p>
                      )}

                      <button
                        type="submit"
                        className="submit-btn"
                        disabled={
                          submittingFeedbackId === complaint.complaintId
                        }
                      >
                        {submittingFeedbackId === complaint.complaintId
                          ? 'Submitting...'
                          : 'Submit Feedback'}
                      </button>
                    </form>
                  )}
                </div>
              )
            })
        )}
      </>
    )}

    <hr />

    <h3>My Submitted Feedback</h3>
    <select className="filter-select" value={feedbackPageSize} onChange={event=>{setFeedbackPageSize(Number(event.target.value));setFeedbackPage(1)}}><option value={10}>10 per page</option><option value={25}>25 per page</option></select>

    {feedbackLoading ? (
      <p>Loading feedback history...</p>
    ) : feedbacks.length === 0 ? (
      <p>You haven't submitted any feedback yet.</p>
    ) : (
      visibleFeedbacks.map(feedback => (
        <div
          className="welcome"
          key={feedback.feedbackId}
        >
          <h3>Complaint #{feedback.complaintId}</h3>

          <p>
            <strong>Rating:</strong>{' '}
            {'⭐'.repeat(Number(feedback.rating) || 0)}
            {' '}({feedback.rating}/5)
          </p>

          <p>
            <strong>Comment:</strong>{' '}
            {feedback.comment || 'No comment'}
          </p>
          {feedback.photoPath && (
  <FeedbackPhoto feedbackId={feedback.feedbackId} />
)}

          <p>
            <strong>Submitted:</strong>{' '}
            {feedback.submittedAt
              ? new Date(feedback.submittedAt).toLocaleString()
              : '-'}
          </p>
        </div>
      ))
    )}
    <Pagination page={feedbackPage} total={feedbacks.length} pageSize={feedbackPageSize} onChange={setFeedbackPage} />
  </>
)}

          </section>
        )}

      </main>

      {/* COMPLAINT DETAILS MODAL */}
      {selectedComplaint && (
        <div
          className="modal-overlay"
          onClick={() => setSelectedComplaint(null)}
        >
          <div
            className="modal-content"
            onClick={event => event.stopPropagation()}
          >
            <div className="modal-header">
              <h2>Complaint Details</h2>

              <button
                className="modal-close"
                onClick={() => setSelectedComplaint(null)}
              >
                ×
              </button>
            </div>

            <div className="detail-row">
              <strong>Complaint ID:</strong>
              <span>{selectedComplaint.complaintId}</span>
            </div>

            <div className="detail-row">
              <strong>Customer Name:</strong>
              <span>{selectedComplaint.customerName}</span>
            </div>

            <div className="detail-row">
              <strong>Address:</strong>
              <span>{selectedComplaint.address}</span>
            </div>

            <div className="detail-row">
              <strong>Complaint Type:</strong>
              <span>{selectedComplaint.complaintType}</span>
            </div>

            <div className="detail-row">
              <strong>Description:</strong>
              <span>{selectedComplaint.description}</span>
            </div>
            {/* Complaint Photo */}
<ComplaintPhoto
  complaintId={selectedComplaint.complaintId}
/>
<CompletionPhoto complaintId={selectedComplaint.complaintId} />
{selectedComplaint.completionRemarks && <div className="detail-row"><strong>Completion remarks:</strong><span>{selectedComplaint.completionRemarks}</span></div>}

            <div className="detail-row">
              <strong>Priority:</strong>
              <span className={getPriorityClass(selectedComplaint.priority)}>
                {selectedComplaint.priority || 'Medium'}
              </span>
            </div>

            <div className="detail-row">
              <strong>Status:</strong>
              <span className={getStatusClass(selectedComplaint.status)}>
                {selectedComplaint.status || 'Pending'}
              </span>
            </div>
            {isCitizen && selectedComplaint.status === 'Resolved' && (
              <WorkerFeedbackDisplay complaintId={selectedComplaint.complaintId} />
            )}
            <hr />

<h3>Complaint Status History</h3>

{historyLoading ? (
  <p>Loading complaint history...</p>
) : historyError ? (
  <p className="error">{historyError}</p>
) : complaintHistory.length === 0 ? (
  <p>No status history available.</p>
) : (
  <div className="history-timeline">
    {complaintHistory.map(item => (
      <div className="history-item" key={item.historyId}>
        <div className="history-marker"></div>

        <div className="history-content">
          <h4>
            {item.oldStatus} → {item.newStatus}
          </h4>

          <p>{item.remarks || 'Status updated'}</p>

          <small>
            Updated by: {item.updatedBy}
          </small>

          <br />

          <small>
            {item.updatedAt
              ? new Date(item.updatedAt).toLocaleString()
              : '-'}
          </small>
        </div>
      </div>
    ))}
  </div>
)}

            <button
              className="submit-btn modal-close-button"
              onClick={() => setSelectedComplaint(null)}
            >
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
