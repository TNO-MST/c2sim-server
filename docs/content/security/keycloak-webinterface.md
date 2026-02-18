# Keycloak web interface

Once the `docker compose up` is completed, the keycloak server is available under `http://localhost:8080'. 

Open an web browser (that support showing unsecure web pages) and login with `admin`, `admin`.

![](keycloak_images\Keycloak_login.png) 

By default the `master` realm is shown, switch to the `c2sim` realm context.

![](keycloak_images\Keycloak_select_realm.png)

In the `client scopes` section, the `c2sim` scope should be added, with all `c2sim claims`.

![](keycloak_images\Keycloak_scope_c2sim_claims.png)

The `c2sim scope` is added to the `client scopes`:

![](keycloak_images\Keycloak_scope_c2sim.png)

In the `clients` section, all clients are visible.

![](keycloak_images\Keycloak_clients.png)
