
import { useState } from 'react'
import API from '../api'

function WorkerCompletionForm({ complaint, onSuccess }) {
  const [status, setStatus] = useState(complaint.status === 'Assigned' ? 'In Progress' : complaint.status || 'In Progress')
  const [photo, setPhoto] = useState(null)
  const [remarks, setRemarks] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    if (status === 'Resolved' && !photo) {
      setError('Completion photo is required.')
      return
    }

    const formData = new FormData()
    formData.append('status', status)

    if (status === 'Resolved') {
      formData.append('photo', photo)
      formData.append('remarks', remarks)
    }

    setLoading(true)

    try {
      await API.put(
        `/worker/complaints/${complaint.complaintId}/status`,
        formData
      )

      setPhoto(null)
      setRemarks('')

      if (onSuccess) await onSuccess()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        'Unable to update complaint.'
      )
    } finally {
      setLoading(false)
    }
  }

  if (complaint.status === 'Resolved') {
    return (
      <div className="worker-resolved" role="status" title="Completion photo and remarks have been shared with the citizen.">
        <span className="worker-resolved-icon" aria-hidden="true">✓</span>
        <span>Completion shared</span>
      </div>
    )
  }

  return (
    <form className="worker-completion-form" onSubmit={handleSubmit}>
      <div className="worker-form-group">
        <label>Update Status</label>
        <select
          value={status}
          onChange={(event) => setStatus(event.target.value)}
        >
          {complaint.status === 'Assigned' ? <option value="In Progress">In Progress</option> : <>
            <option value="In Progress">In Progress</option>
            <option value="Resolved">Resolved</option>
          </>}
        </select>
      </div>

      {status === 'Resolved' && (
        <div className="worker-resolution-fields">
          <div className="worker-form-group">
            <label>Completion Photo (Required)</label>
            <input
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(event) =>
                setPhoto(event.target.files?.[0] || null)
              }
              required
            />
          </div>

          <div className="worker-form-group">
            <label>Completion Remarks</label>
            <textarea
              value={remarks}
              onChange={(event) => setRemarks(event.target.value)}
              placeholder="Describe the completed work"
              maxLength="1000"
            />
          </div>
        </div>
      )}

      {error && <p className="worker-form-error">{String(error)}</p>}

      <button
        type="submit"
        className="worker-update-btn"
        disabled={loading}
      >
        {loading ? 'Updating...' : 'Update Status'}
      </button>
    </form>
  )
}

export default WorkerCompletionForm
