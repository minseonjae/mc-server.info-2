const ctx = document.getElementById("chart").getContext("2d");
new Chart(ctx, {
    type: 'line',
    data: {
        labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'],
        datasets: [{
            label: 'players',
            data: [12, 19, 3, 5, 2, 3],
            backgroundColor: [
                'rgba(0, 0, 0, 1)'
            ],
            borderColor: [
                'rgba(0, 0, 0, 1)'
            ],
            radius: 1,
            borderWidth: 2
        }]
    },
    options: {
        plugins: {
            legend: {
                display: false
            },
            tooltip: {
                callbacks: {
                    title: function () {}
                }
            }
        },
        scales: {
            x: {
                grid: {
                    display: false
                },
                ticks: {
                    display: false
                }
            },
            y: {
                grid: {
                    display: false
                },
                ticks: {
                    display: false
                },
                beginAtZero: true
            }
        }
    }
});