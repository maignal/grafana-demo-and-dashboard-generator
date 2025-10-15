# Grafana Demo and Dashboard Generator

Docker Compose setup and Dashboard Generator for demonstrating alerting features in Grafana.


## Dashboard generator

The `dashboard-generator` subproject contains small Java programs that emit Grafana JSON. A new generator for the demo `cpu_usage` metric is available at:

 - `dashboard-generator/src/main/java/cpu-monitoring/CpuDashboardGenerator.java`


To build and run the generator (replace the datasource UID if needed):

```sh
cd dashboard-generator
./gradlew run
```

The program prints the dashboard JSON to stdout.

## Docker Compose setup 
This repository includes a [Docker Compose setup](./docker-compose.yaml) that runs Grafana, Prometheus, Prometheus Alertmanager, Loki, and an SMTP server for testing email notifications.

To run the demo environment :

```bash
docker compose up -d
```

You might need to execute the following command if encounter an issue with smtp.env.example.

```
cp environments/smtp.env.example environments/smtp.env
```

You can then access:
- Grafana: [http://localhost:3000](http://localhost:3000/)
- Prometheus web UI: [http://localhost:9090](http://localhost:9090/)
- Alertmanager web UI: [http://localhost:9093](http://localhost:9093/)

### Generating test data

This demo uses [Grafana k6](https://grafana.com/docs/k6) to generate test data for Prometheus and Loki.

The [k6 tests in the `testdata` folder](./testdata/) inject Prometheus metrics and Loki logs that you can use to define alert queries and conditions. 

1. Install **k6 v1.2.0** or later.

2. Run a k6 test with the following command:

    ```bash
    k6 run testdata/<FILE>.js
    ```

You can modify and run the k6 scripts to simulate different alert scenarios.
For details on inserting data into Prometheus or Loki, see the `xk6-client-prometheus-remote` and `xk6-loki` APIs.

[For more information about Demo Alerting in Prometheus and Grafana, see the original README from the source repo](https://github.com/grafana/demo-prometheus-and-grafana-alerts/blob/main/README.md)

## Display the dashboard in Grafana

The json file describing the dashboard that Main.java generates is already part of the repo. Therefore, when setting up Docker Compose and k6, it should normally appear in grafana.

You can also import the raw json output in the dashboard manager http://localhost:3000/dashboard/import 

Or you can execute the following command. It will excract the json text from the generator and paste it in a json file `grafana/dashboards/definitions`. Inside `dashboard-generator` :

```
echo "$(./gradlew run)" | sed -n '/^{/,/^}$/p' | tr -d '\n' > ../grafana/dashboards/definitions/cpu_monitoring.json
```

After the `cpu_monitoring.json` file has been created, you will need to run the demo environment again.