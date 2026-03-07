# Metrics

!!! warning

Auto-generated documentation

## Metric "c2sim_client_msg_bytes_sent_total"

**Type:** counter  
**Description:** Total size in bytes received (C2SIM XML MSG) by C2SIM server from C2SIM client (system)

**Labels:** c2sim_app, c2sim_shared_session, c2sim_system_name

## Metric "c2sim_client_msg_invalid_count_total"

**Type:** counter  
**Description:** Number of invalid C2SIM messages send by system (C2SIM client) for a shared session

**Labels:** c2sim_app, c2sim_error_kind, c2sim_shared_session, c2sim_system_name

## Metric "c2sim_client_msg_valid_count_total"

**Type:** counter  
**Description:** Number of C2SIM messages send by system (C2SIM client) for a shared session

**Labels:** c2sim_app, c2sim_msg_kind, c2sim_shared_session, c2sim_system_name

## Metric "c2sim_request_duration_seconds"

**Type:** histogram  
**Description:** C2SIM HTTP request duration for all endpoints

**Labels:** c2sim_app, c2sim_request_endpoint, c2sim_request_method, c2sim_request_status

## Metric "c2sim_request_duration_seconds_max"

**Type:** gauge  
**Description:** C2SIM HTTP request duration for all endpoints

**Labels:** c2sim_app, c2sim_request_endpoint, c2sim_request_method, c2sim_request_status

## Metric "c2sim_server_active_requests"

**Type:** gauge  
**Description:**

**Labels:** c2sim_app

## Metric "disk_free_bytes"

**Type:** gauge  
**Description:** Usable space for path

**Labels:** c2sim_app, path

## Metric "disk_total_bytes"

**Type:** gauge  
**Description:** Total space for path

**Labels:** c2sim_app, path

## Metric "jetty_connections_bytes_in_bytes"

**Type:** summary  
**Description:** Bytes received by tracked connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_bytes_in_bytes_max"

**Type:** gauge  
**Description:** Bytes received by tracked connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_bytes_out_bytes"

**Type:** summary  
**Description:** Bytes sent by tracked connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_bytes_out_bytes_max"

**Type:** gauge  
**Description:** Bytes sent by tracked connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_current_connections"

**Type:** gauge  
**Description:** The current number of open Jetty connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_max_connections"

**Type:** gauge  
**Description:** The maximum number of observed connections over a rolling 2-minute interval

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_messages_in_messages_total"

**Type:** counter  
**Description:** Messages received by tracked connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_messages_out_messages_total"

**Type:** counter  
**Description:** Messages sent by tracked connections

**Labels:** c2sim_app, connector_name

## Metric "jetty_connections_request_seconds"

**Type:** summary  
**Description:** Jetty client or server requests

**Labels:** c2sim_app, connector_name, type

## Metric "jetty_connections_request_seconds_max"

**Type:** gauge  
**Description:** Jetty client or server requests

**Labels:** c2sim_app, connector_name, type

## Metric "jetty_server_async_dispatches_total"

**Type:** counter  
**Description:** Asynchronous dispatches

**Labels:** c2sim_app

## Metric "jetty_server_async_expires_total"

**Type:** counter  
**Description:** Asynchronous operations that timed out before completing

**Labels:** c2sim_app

## Metric "jetty_server_async_waits_operations"

**Type:** gauge  
**Description:** Pending asynchronous wait operations

**Labels:** c2sim_app

## Metric "jetty_server_dispatches_open_seconds"

**Type:** summary  
**Description:** Jetty dispatches that are currently in progress

**Labels:** c2sim_app

## Metric "jetty_server_dispatches_open_seconds_max"

**Type:** gauge  
**Description:** Jetty dispatches that are currently in progress

**Labels:** c2sim_app

## Metric "jetty_server_requests_seconds"

**Type:** summary  
**Description:** HTTP requests to the Jetty server

**Labels:** c2sim_app, exception, method, outcome, status, uri

## Metric "jetty_server_requests_seconds_max"

**Type:** gauge  
**Description:** HTTP requests to the Jetty server

**Labels:** c2sim_app, exception, method, outcome, status, uri

## Metric "jetty_threads_busy"

**Type:** gauge  
**Description:** The number of busy threads in the pool

**Labels:** c2sim_app

## Metric "jetty_threads_config_max"

**Type:** gauge  
**Description:** The maximum number of threads in the pool

**Labels:** c2sim_app

## Metric "jetty_threads_config_min"

**Type:** gauge  
**Description:** The minimum number of threads in the pool

**Labels:** c2sim_app

## Metric "jetty_threads_current"

**Type:** gauge  
**Description:** The total number of threads in the pool

**Labels:** c2sim_app

## Metric "jetty_threads_idle"

**Type:** gauge  
**Description:** The number of idle threads in the pool

**Labels:** c2sim_app

## Metric "jetty_threads_jobs"

**Type:** gauge  
**Description:** Number of jobs queued waiting for a thread

**Labels:** c2sim_app

## Metric "jvm_buffer_count_buffers"

**Type:** gauge  
**Description:** An estimate of the number of buffers in the pool

**Labels:** c2sim_app, id

## Metric "jvm_buffer_memory_used_bytes"

**Type:** gauge  
**Description:** An estimate of the memory that the Java virtual machine is using for this buffer pool

**Labels:** c2sim_app, id

## Metric "jvm_buffer_total_capacity_bytes"

**Type:** gauge  
**Description:** An estimate of the total capacity of the buffers in this pool

**Labels:** c2sim_app, id

## Metric "jvm_memory_committed_bytes"

**Type:** gauge  
**Description:** The amount of memory in bytes that is committed for the Java virtual machine to use

**Labels:** area, c2sim_app, id

## Metric "jvm_memory_max_bytes"

**Type:** gauge  
**Description:** The maximum amount of memory in bytes that can be used for memory management

**Labels:** area, c2sim_app, id

## Metric "jvm_memory_used_bytes"

**Type:** gauge  
**Description:** The amount of used memory

**Labels:** area, c2sim_app, id

## Metric "jvm_threads_daemon_threads"

**Type:** gauge  
**Description:** The current number of live daemon threads

**Labels:** c2sim_app

## Metric "jvm_threads_live_threads"

**Type:** gauge  
**Description:** The current number of live threads including both daemon and non-daemon threads

**Labels:** c2sim_app

## Metric "jvm_threads_peak_threads"

**Type:** gauge  
**Description:** The peak live thread count since the Java virtual machine started or peak was reset

**Labels:** c2sim_app

## Metric "jvm_threads_started_threads_total"

**Type:** counter  
**Description:** The total number of application threads started in the JVM

**Labels:** c2sim_app

## Metric "jvm_threads_states_threads"

**Type:** gauge  
**Description:** The current number of threads

**Labels:** c2sim_app, state

## Metric "process_cpu_time_ns_total"

**Type:** counter  
**Description:** The "cpu time" used by the Java Virtual Machine process

**Labels:** c2sim_app

## Metric "process_cpu_usage"

**Type:** gauge  
**Description:** The "recent cpu usage" for the Java Virtual Machine process

**Labels:** c2sim_app

## Metric "process_start_time_seconds"

**Type:** gauge  
**Description:** Start time of the process since unix epoch.

**Labels:** c2sim_app

## Metric "process_uptime_seconds"

**Type:** gauge  
**Description:** The uptime of the Java virtual machine

**Labels:** c2sim_app

## Metric "system_cpu_count"

**Type:** gauge  
**Description:** The number of processors available to the Java virtual machine

**Labels:** c2sim_app

## Metric "system_cpu_usage"

**Type:** gauge  
**Description:** The "recent cpu usage" of the system the application is running in

**Labels:** c2sim_app

## Metric "system_load_average_1m"

**Type:** gauge  
**Description:** The sum of the number of runnable entities queued to available processors and the number of runnable entities running on the available processors averaged over a period of time

**Labels:** c2sim_app
