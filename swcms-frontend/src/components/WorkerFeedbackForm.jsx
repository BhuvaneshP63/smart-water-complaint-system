
import { useEffect, useState } from 'react'
import API from '../api'
import FeedbackPhoto from './FeedbackPhoto'
import './WorkerFeedbackForm.css'

function WorkerFeedbackForm({ complaint, onSuccess, onClose, onFeedbackSubmitted }) {
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [photo, setPhoto] = useState(null)

  const [loading, setLoading] = useState(false)
  const [checking, setChecking] = useState(true)
  const [feedbackSubmitted, setFeedbackSubmitted] = useState(false)
  const [existingFeedback, setExistingFeedback] = useState(null)

  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    let active = true

    const checkFeedback = async () => {
      try {
        const response = await API.get(
          `/worker/complaints/${complaint.complaintId}/feedback`
        )

        if (active) {
          setExistingFeedback(response.data)
          setFeedbackSubmitted(true)
          setError('')
        }
      } catch (err) {
        if (active) {
          const status = err.response?.status

          if (status === 404) {
            // No feedback has been submitted yet.
            setFeedbackSubmitted(false)
            setExistingFeedback(null)
            setError('')
          } else {
            setError(
              err.response?.data?.message ||
              (typeof err.response?.data === 'string'
                ? err.response.data
                : 'Unable to check existing feedback.')
            )
          }
        }
      } finally {
        if (active) setChecking(false)
      }
    }

    checkFeedback()

    return () => {
      active = false
    }
  }, [complaint.complaintId])

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setSuccess('')

    const formData = new FormData()
    formData.append('rating', rating)
    formData.append('comment', comment)

    if (photo) {
      formData.append('photo', photo)
    }

    setLoading(true)

    try {
      const response = await API.post(
        `/worker/complaints/${complaint.complaintId}/feedback`,
        formData
      )

      const savedFeedback = response.data || {
        rating,
        comment
      }
      setExistingFeedback(savedFeedback)
      setFeedbackSubmitted(true)
      setSuccess('Feedback sent to the citizen.')
      if (onFeedbackSubmitted) onFeedbackSubmitted(savedFeedback)

      setComment('')
      setPhoto(null)

      // Submission is already committed. A stats refresh failure must not
      // make the worker think the feedback failed or expose the form again.
      if (onSuccess) await onSuccess()
    } catch (err) {
      setError(
        err.response?.data?.message ||
        (typeof err.response?.data === 'string'
          ? err.response.data
          : 'Unable to submit feedback.')
      )
    } finally {
      setLoading(false)
    }
  }

  if (checking) return <p>Checking feedback...</p>

  if (error && !feedbackSubmitted) {
    return <p className="error">{error}</p>
  }

  if (feedbackSubmitted && existingFeedback) {
    return (
      <div className="worker-feedback-form worker-feedback-confirmation" role="status">
        <div className="worker-feedback-heading">
          <span className="eyebrow">COMPLAINT #{complaint.complaintId}</span>
          <h3>Feedback sent to the citizen</h3>
          <p>Your rating and message are saved. You can submit feedback once for this complaint.</p>
        </div>
        <p><strong>Rating:</strong> {existingFeedback.rating}/5</p>
        <p><strong>Comment:</strong> {existingFeedback.comment || 'No comment'}</p>
        {existingFeedback.photoPath && <FeedbackPhoto feedbackId={existingFeedback.feedbackId} endpoint={`/worker-feedback/${existingFeedback.feedbackId}/photo`} />}
        {onClose && <button type="button" className="worker-feedback-close" onClick={onClose}>Close</button>}
      </div>
    )
  }

  return (
    <form className="worker-feedback-form" onSubmit={handleSubmit}>
      <div className="worker-feedback-heading">
        <span className="eyebrow">COMPLAINT #{complaint.complaintId}</span>
        <h3>Send feedback to the citizen</h3>
        <p>This is a one-time message for this complaint, separate from the completion remarks and photo.</p>
      </div>

      <div className="worker-feedback-field worker-feedback-rating">
        <label htmlFor={`worker-feedback-rating-${complaint.complaintId}`}>Rating</label>
        <select
          id={`worker-feedback-rating-${complaint.complaintId}`}
          value={rating}
          onChange={(e) => setRating(Number(e.target.value))}
        >
          <option value={5}>5 · Excellent</option>
          <option value={4}>4 · Very good</option>
          <option value={3}>3 · Good</option>
          <option value={2}>2 · Fair</option>
          <option value={1}>1 · Poor</option>
        </select>
      </div>

      <div className="worker-feedback-field worker-feedback-comment">
        <label htmlFor={`worker-feedback-comment-${complaint.complaintId}`}>Comment <span>Optional</span></label>
        <textarea
          id={`worker-feedback-comment-${complaint.complaintId}`}
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="Add a short note about the completed work…"
          maxLength={1000}
          rows={3}
        />
        <small>{comment.length}/1000</small>
      </div>

      <div className="worker-feedback-field worker-feedback-photo-field">
        <label htmlFor={`worker-feedback-photo-${complaint.complaintId}`}>Photo <span>Optional · JPG, PNG or WebP</span></label>
        <input
          id={`worker-feedback-photo-${complaint.complaintId}`}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          onChange={(e) => setPhoto(e.target.files?.[0] || null)}
        />
        {photo && <small className="worker-feedback-filename">Selected: {photo.name}</small>}
      </div>

      {(error || success) && <p className={`worker-feedback-notice ${error ? 'is-error' : 'is-success'}`} role="status">{error || success}</p>}

      <div className="worker-feedback-actions">
        <button type="submit" disabled={loading}>
          {loading ? 'Submitting…' : 'Submit feedback'}
        </button>
        <small>You can submit feedback once for this complaint.</small>
        {onClose && <button type="button" className="worker-feedback-cancel" onClick={onClose} disabled={loading}>Cancel</button>}
      </div>
    </form>
  )
}

export default WorkerFeedbackForm
