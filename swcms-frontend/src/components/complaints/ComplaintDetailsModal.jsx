
import ComplaintPhoto from '../ComplaintPhoto'

function ComplaintDetailsModal({ complaint, onClose }) {
  if (!complaint) return null

  const fields = [
    ["Complaint ID", complaint.complaintId],
    ["Customer Name", complaint.customerName],
    ["Address", complaint.address],
    ["Complaint Type", complaint.complaintType],
    ["Description", complaint.description],
    ["Priority", complaint.priority],
    ["Status", complaint.status]
  ]

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2>Complaint Details</h2>

          <button
            className="modal-close"
            onClick={onClose}
          >
            ×
          </button>
        </div>

        <div className="complaint-details">
          {fields.map(([label, value]) => (
            <div className="detail-row" key={label}>
              <strong>{label}:</strong>
              <span>{value || "-"}</span>
            </div>
          ))}
        </div>

        {/* Complaint Photo */}
        <ComplaintPhoto
          complaintId={complaint.complaintId}
        />

        <button
          className="submit-btn"
          onClick={onClose}
        >
          Close
        </button>
      </div>
    </div>
  )
}

export default ComplaintDetailsModal
