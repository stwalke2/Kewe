import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchBusinessObjectTypes, fetchBusinessObjects } from '../api';
import type { BusinessObjectInstance, BusinessObjectType } from '../api/types';

export function BusinessObjectsPage() {
  const [typeCode, setTypeCode] = useState('');
  const [types, setTypes] = useState<BusinessObjectType[]>([]);
  const [objects, setObjects] = useState<BusinessObjectInstance[]>([]);
  const navigate = useNavigate();
  useEffect(() => { void fetchBusinessObjectTypes().then(setTypes); }, []);
  useEffect(() => { void fetchBusinessObjects(typeCode || undefined).then(setObjects); }, [typeCode]);
  return <section className='page-section'><h2>Business Objects</h2><label>Filter by type<select value={typeCode} onChange={(e)=>setTypeCode(e.target.value)}><option value=''>All</option>{types.map((t)=><option key={t.code} value={t.code}>{t.name}</option>)}</select></label><table><thead><tr><th>Code</th><th>Name</th><th>Type</th><th>Status</th></tr></thead><tbody>{objects.map((o)=><tr key={o.id} onClick={()=>navigate(`/business-objects/${o.id}`)}><td>{o.code}</td><td>{o.name}</td><td>{o.typeCode}</td><td>{o.status}</td></tr>)}</tbody></table></section>;
}
