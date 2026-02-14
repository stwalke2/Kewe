# System Specification

## 1. Purpose

Build a modern education-focused ERP platform that:
- Uses canonical object modeling
- Treats transactions as events
- Treats agents as role-bearing entities
- Supports flexible hierarchies and dimensions
- Is modular, extensible, and audit-first

## 2. Scope (Phase 1 – MVP)

The MVP will include:
- One canonical object
- One transaction workflow
- Audit logging
- Basic role-based access
- MongoDB persistence

## 3. Non-Goals (For Now)

- UI polish
- Complex integrations
- Multi-tenant scaling
- Performance optimization

## 4. Core Design Concepts

- Everything is an object
- Transactions are financial-flavored events
- Agents are entities with roles
- All objects are audit-tracked
- Effective dating supported where needed

## 5. MVP Slice

- Supplier Invoice
