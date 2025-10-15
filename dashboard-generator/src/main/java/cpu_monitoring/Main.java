package cpu_monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.grafana.foundation.dashboard.*;
import com.grafana.foundation.prometheus.DataqueryBuilder;
import com.grafana.foundation.timeseries.PanelBuilder;
import com.grafana.foundation.units.Constants;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static com.grafana.foundation.common.Constants.TimeZoneBrowser;

public class Main {

    public static void main(String[] args) throws IOException, JsonProcessingException {
        // Build the dashboard
        DashboardBuilder dashboard = new DashboardBuilder("[Example] CPU Usage Dashboard")
                .uid("example-cpu-usage")
                .tags(List.of("generated", "cpu", "monitoring"))
                .editable()
                .tooltip(DashboardCursorSync.CROSSHAIR)
                .refresh("5s")
                .time(new DashboardDashboardTimeBuilder()
                        .from("now-15m")
                        .to("now"))
                .timezone(TimeZoneBrowser)
                .timepicker(new TimePickerBuilder()
                        .refreshIntervals(List.of("5s", "10s", "30s", "1m", "5m", "15m", "30m", "1h", "2h", "1d")));

        // Row 1 - High-level CPU overview
        dashboard.withRow(new RowBuilder("Overall CPU"))
                .withPanel(overallCpuUsagePanel().span(24).height(8));

        // Row 2 - Breakdown by instance
        dashboard.withRow(new RowBuilder("Per Instance CPU Usage"))
                .withPanel(perInstanceCpuPanel().span(24).height(8));

        // Row 3 - CPU load averages
        dashboard.withRow(new RowBuilder("System Load"))
                .withPanel(loadAveragePanel().span(24).height(8));

        // Row 4 - 5-minute rolling average of cpu_usage (synthetic metric)
        dashboard.withRow(new RowBuilder("5m Average CPU Usage"))
                .withPanel(cpuUsageAveragePanel().span(24).height(8));


        // Print to console
        try {
            System.out.println(dashboard.build().toJSON());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private static PanelBuilder overallCpuUsagePanel() {
        return new PanelBuilder()
                .title("CPU Usage (from custom metric)")
                .description("Current CPU usage percentage based on 'cpu_usage' metric")
                .datasource(new DataSourceRef("Prometheus", "DS_PROMETHEUS_UID"))
                .unit(Constants.Percent)
                .withTarget(new DataqueryBuilder()
                        .expr("cpu_usage")
                        .legendFormat("{{instance}}"));
    }

    private static PanelBuilder perInstanceCpuPanel() {
        return new PanelBuilder()
                .title("Per Instance CPU Usage")
                .description("Compare CPU usage between instances")
                .datasource(new DataSourceRef("Prometheus", "DS_PROMETHEUS_UID"))
                .unit(Constants.Percent)
                .withTarget(new DataqueryBuilder()
                        .expr("avg by(instance)(cpu_usage)")
                        .legendFormat("{{instance}}"));
    }

    private static PanelBuilder loadAveragePanel() {
        return new PanelBuilder()
                .title("System Load (1m, from node exporter if available)")
                .description("Shows system load average over 1 minute")
                .datasource(new DataSourceRef("Prometheus", "DS_PROMETHEUS_UID"))
                .unit(Constants.Percent)
                .withTarget(new DataqueryBuilder()
                        .expr("node_load1")
                        .legendFormat("Load 1m {{instance}}"));
    }

    private static PanelBuilder cpuUsageAveragePanel() {
        return new PanelBuilder()
                .title("Average CPU Usage (5m window)")
                .description("Rolling 5-minute average of cpu_usage")
                .datasource(new DataSourceRef("Prometheus", "DS_PROMETHEUS_UID"))
                .unit(Constants.Percent)
                .withTarget(new DataqueryBuilder()
                        .expr("avg_over_time(cpu_usage[5m])")
                        .legendFormat("5m avg {{instance}}"));
    }
}
