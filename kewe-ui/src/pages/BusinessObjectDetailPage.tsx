import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { fetchBusinessObject, fetchBusinessObjectType, updateBusinessObjectOverrides } from '../api';
import type { BusinessObjectInstance, BusinessObjectType } from '../api/types';
import { HelpTip } from '../ui/help/HelpTip';

const fields = ['budgetRequired','budgetControlLevel','allowBudgetOverride','allowCarryforward','allowExpensePosting','allowRevenuePosting','enableEncumbrance','enablePreEncumbrance','defaultLedgerAccountId','defaultFunctionId','restrictionType','netAssetClassMapping','idcEligible','sponsorApprovalRequired','allowCostTransfer','cashManaged','investmentManaged','unitized','capitalizable','defaultDepreciationProfile'] as const;

export function BusinessObjectDetailPage(){
  const {id=''} = useParams();
  const [obj,setObj]=useState<BusinessObjectInstance|null>(null);
  const [type,setType]=useState<BusinessObjectType|null>(null);
  useEffect(()=>{void (async()=>{const o=await fetchBusinessObject(id); setObj(o); setType(await fetchBusinessObjectType(o.typeCode));})();},[id]);
  if(!obj || !type) return <p>Loading…</p>;
  const overrides = obj.accountingBudgetOverrides ?? {};
  return <section className='page-section'><div className='page-header-row'><h2>Business Object Instance: {obj.code}</h2><button className='btn btn-primary' onClick={()=>void updateBusinessObjectOverrides(obj.id, Object.fromEntries(Object.entries(obj.accountingBudgetOverrides ?? {}).filter(([,v])=>v)))}>Save</button></div><p>Values shown as Default come from the Business Object Type. You may override fields only where allowed. Overrides may require justification.</p><div className='form-grid'>{fields.map((f)=>{const cfg=type.accountingBudgetDefaults?.[f]; const ov=overrides[f]; const canOverride=Boolean(cfg?.allowOverride); return <div key={f} className='card'><div>{f} <HelpTip term={f} /> {ov ? <span className='status-pill pending'>Overridden</span>:null}</div><div>Default: {String(cfg?.defaultValue ?? '—')}</div><div>Effective: {String(ov?.value ?? cfg?.defaultValue ?? '—')}</div>{canOverride && <><label className='inline-check'><input type='checkbox' checked={Boolean(ov)} onChange={(e)=>setObj({...obj,accountingBudgetOverrides:{...overrides,[f]:e.target.checked?{value:cfg?.defaultValue ?? ''}:undefined as never}})}/> Override</label>{ov && <><input value={String(ov.value)} onChange={(e)=>setObj({...obj,accountingBudgetOverrides:{...overrides,[f]:{...ov,value:e.target.value}}})}/>{cfg?.overrideReasonRequired && <input placeholder='Override reason' value={ov.overrideReason ?? ''} onChange={(e)=>setObj({...obj,accountingBudgetOverrides:{...overrides,[f]:{...ov,overrideReason:e.target.value}}})}/>}</>}</>}</div>;})}</div><button className='btn btn-primary' onClick={()=>void updateBusinessObjectOverrides(obj.id, Object.fromEntries(Object.entries(obj.accountingBudgetOverrides ?? {}).filter(([,v])=>v)))}>Save</button></section>
}
