import { useEffect, useState } from 'react'
import API from '../api'

export default function CompletionPhoto({ complaintId }) {
  const [photoUrl, setPhotoUrl] = useState('')
  const [expanded, setExpanded] = useState(false)
  useEffect(() => {
    let active=true; let objectUrl
    API.get(`/complaints/${complaintId}/completion-photo`,{responseType:'blob'})
      .then(response => { objectUrl=URL.createObjectURL(response.data); if(active)setPhotoUrl(objectUrl); else URL.revokeObjectURL(objectUrl) })
      .catch(() => { if(active)setPhotoUrl('') })
    return () => { active=false; if(objectUrl)URL.revokeObjectURL(objectUrl) }
  },[complaintId])
  if(!photoUrl)return null
  return <div className="complaint-photo"><h4>Work completion photo</h4><img src={photoUrl} alt={`Completion proof for complaint ${complaintId}`} onClick={()=>setExpanded(true)} style={{width:220,maxWidth:'100%',height:160,objectFit:'cover',borderRadius:8,cursor:'zoom-in'}}/>
    {expanded&&<div className="photo-lightbox" onClick={()=>setExpanded(false)}><button aria-label="Close photo" onClick={()=>setExpanded(false)}>×</button><img src={photoUrl} alt={`Expanded completion proof ${complaintId}`} onClick={event=>event.stopPropagation()}/></div>}
  </div>
}
