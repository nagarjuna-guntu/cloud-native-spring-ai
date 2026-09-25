
## System Architecture & Design
The BookShop ecosystem is built on a modern, cloud-native blueprint leveraging Spring Boot microservices, asynchronous event-driven choreographies, 
and an autonomous AI agentic layer driven by Spring AI and the Model Context Protocol (MCP).

## 1. Stateless Microservices

*
* edge-service (API Gateway)
* Technology: Spring Cloud WebFlux (Reactive Stack)
   * Role: Single entry point for all system ingress traffic. It manages intelligent routing rules and applies bulkhead, retry, and circuit-breaker
   * patterns using Resilience4j to ensure system wide fault tolerance. Routes traffic seamlessly to catalog-service, order-service, and agent-service.
* agent-service (AI Orchestrator & Client)
* Technology: Spring Boot + Spring AI + MCP Client (Stateless HTTP Sync) + Ollama (LLM)
   * Role: Acts as the cognitive engine of the shop. Orchestrates natural language requests into operational functions by invoking tools exposed
   * by downstream MCP servers based on LLM execution graphs.
* catalog-service (Inventory & Semantics)
* Technology: Spring Boot + Spring AI + MCP Server (Stateless HTTP Sync) + Ollama Embeddings
   * Role: Manages core catalog inventory. Operates as an MCP server exposing the Search Book tool (utilizing vector distance algorithms
   * for deep thematic similarities instead of strict keyword lookups).
* order-service (Sales & Ledger)
* Technology: Spring Boot + Spring AI + MCP Server (Stateless HTTP Sync)
   * Role: Manages shopping cart conversions, checkouts, and order history inquiries. Exposes the place order and find order tools as an MCP Server
   * to allow autonomous processing from the agent tier.
* dispatch-service (Logistics Engine)
* Technology: Spring Boot + Spring Cloud Stream
   * Role: Completely reactive post-purchase operational worker. Listens exclusively to event topologies to orchestrate material shipping
   * and order fulfillment schedules.
*

------------------------------
## 2. Stateful Infrastructure Layer
The persistence and streaming network utilizes a strict Database-per-Service structural pattern to ensure zero runtime coupling between service boundaries.

* 
* PostgreSQL & pgvector: Separate, network-isolated transactional nodes handle schema separation. The catalog-service database includes
* the pgvector extension to convert, store, and score heavy embeddings arrays (Title:Author:Publisher:Price).
* Redis Cluster: Configured as a distributed Cache-Aside cache layer. Holds fast hot-reads mapped into BOOKS:ISBN and BOOKS:ALL collections
* to dramatically shield relational resources from high read throughput.
* RabbitMQ: High-availability event fabric integrated via Spring Cloud Stream. It acts as the eventual consistency backplane for cross-domain
* orchestrations and immediate out-of-band updates.
* 

------------------------------
## Asynchronous Event-Driven Flows## A. Order Fulfillment Lifecycle

[order-service] ──(Saves Order As "Accepted")──> Emits: OrderAccepted Event
                                                         │
                                                         ▼
[dispatch-service] <──(Consumes OrderAccepted)── Process Shipping Logistics
        │
     Emits: OrderDispatched Event
        │
        ▼
[order-service] <──(Consumes OrderDispatched)── Updates Order State to "Dispatched" in DB

## B. Cache & Vector Store Synchronisation
Whenever an inventory change occurs, the catalog-service leverages a self-consuming loop to isolate mutations out-of-band:

   1. A book transaction commits directly to DB.
   2. A Book Created or Book Updated event broadcasts onto RabbitMQ.
   3. catalog-service immediately consumes its own message topology to execute background worker processing:
      * Cache: Invalidates or updates corresponding items within the Redis cluster.
      * Vector Engine: Calls Ollama to recalculate array multi-dimensional metadata and stores the updated array into pgvector.
   
------------------------------
## 3. Enterprise Observability Suite
Comprehensive, system-wide runtime tracking is baked natively into the image binaries:

* 
* Telemetry Pipeline: Microservices include the standard OpenTelemetry (OTel) starter to continuously stream standardized, zero-alloc traces,
* metric dimensions, and semantic logs without manual configuration.
* Monitoring Cluster: Standardized telemetry charts ingest straight into a centralized Grafana LGTM Stack instance:
* Loki: Distributed log aggregation.
   * Tempo: High-scale distributed tracing tracking requests from edge-service down to the DB.
   * Mimir: Long-term time series metrics storage.
   * Grafana: Central visual dashboards mapping out the overall holistic system health.
* 

------------------------------



