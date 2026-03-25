/**
 * Extent Spark Custom JS
 * Modified to add TOTAL column in Tags + QA Name (Abhay)
 */

(function() {

    // ---------- UTILITIES ----------
    function percent(value, total) {
        return total === 0 ? 0 : Math.round((value / total) * 100);
    }

    // ---------- DASHBOARD ----------
    window.renderDashboard = function(data) {

        // counts
        var parent = data.parent;
        var events = data.events;

        // pie charts
        renderDonut('parent-analysis', [
            { label: 'Pass', value: parent.pass },
            { label: 'Fail', value: parent.fail },
            { label: 'Skip', value: parent.skip },
            { label: 'Others', value: parent.others }
        ]);

        renderDonut('events-analysis', [
            { label: 'Pass', value: events.pass },
            { label: 'Fail', value: events.fail },
            { label: 'Info', value: events.info }
        ]);

        // timeline
        renderTimeline(data.timeline);

        // categories table (TAGS)
        renderCategories(data.categoryView);

        // sys/env
        renderSystemEnv(data.systemEnv);
    };


    // ---------- CHARTS ----------
    function renderDonut(id, values) {
        var ctx = document.getElementById(id).getContext('2d');
        var v = values.map(v => v.value);
        var labels = values.map(v => v.label);

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: v,
                    backgroundColor: ['#5cb85c', '#d9534f', '#5bc0de', '#f0ad4e']
                }]
            },
            options: { responsive: true, legend: { display: true } }
        });
    }

    function renderTimeline(timeline) {
        var ctx = document.getElementById("timeline").getContext("2d");
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: Object.keys(timeline),
                datasets: [{
                    label: "Seconds",
                    data: Object.values(timeline),
                    backgroundColor: "#8FBC8F"
                }]
            },
            options: { responsive: true, legend: { display: false } }
        });
    }


    // ---------- TAG TABLE (Categories) ----------
    function renderCategories(categoryView) {

        var html = `
            <thead>
                <tr class="bg-gray">
                    <th>Name</th>
                    <th>Total</th>
                    <th>Passed</th>
                    <th>Failed</th>
                    <th>Skipped</th>
                    <th>Others</th>
                    <th>Passed %</th>
                </tr>
            </thead>
            <tbody>
        `;

        categoryView.categories.forEach(function(c) {

            var total = c.passed + c.failed + c.skipped + c.others;

            html += `
                <tr>
                    <td>${c.name}</td>
                    <td>${total}</td>
                    <td>${c.passed}</td>
                    <td>${c.failed}</td>
                    <td>${c.skipped}</td>
                    <td>${c.others}</td>
                    <td>${percent(c.passed, total)}%</td>
                </tr>
            `;
        });

        html += `</tbody>`;

        document.querySelector(".category-container table").innerHTML = html;
    }


    // ---------- SYSTEM / ENV ----------
    function renderSystemEnv(sysEnv) {

        // Add QA Name dynamically (FIRST ROW)
        sysEnv.unshift({
            name: "QA Name",
            value: "Abhay"
        });

        var html = `
            <thead>
                <tr class="bg-gray">
                    <th>Name</th>
                    <th>Value</th>
                </tr>
            </thead>
            <tbody>
        `;

        sysEnv.forEach(function(env) {
            html += `
                <tr>
                    <td>${env.name}</td>
                    <td>${env.value}</td>
                </tr>
            `;
        });

        html += `</tbody>`;

        document.querySelector(".sysenv-container table").innerHTML = html;
    }

})();
