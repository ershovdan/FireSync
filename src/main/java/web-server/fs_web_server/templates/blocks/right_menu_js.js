async function getDataRightMenu(check) {
    if (check == "total_shares") {
        let response = await fetch("/operations_get_data?check=total_shares", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        total_shares = data
        return data
    }
}

function httpGet(theUrl)
{
    var xmlHttp = new XMLHttpRequest()
    xmlHttp.open("GET", theUrl, false)
    xmlHttp.send( null )
    return xmlHttp.responseText
}

getDataRightMenu("total_shares")

try {
    let t_s = total_shares["answer"]
    total_shares = t_s
    document.getElementById("total_shares").innerHTML = "YOU HAVE " + total_shares + " ACTIVE SHARES"
} catch (error) {}


document.getElementById("graph_network_5min").onclick = function () {
    gr_net_min = 5
    document.getElementById("graph_network_5min").style.backgroundColor = "#c2c2c2"
    document.getElementById("graph_network_15min").style.backgroundColor = "white"
    document.getElementById("graph_network_30min").style.backgroundColor = "white"
    document.getElementById("graph_network_60min").style.backgroundColor = "white"
}
document.getElementById("graph_network_15min").onclick = function () {
    gr_net_min = 15
    document.getElementById("graph_network_15min").style.backgroundColor = "#c2c2c2"
    document.getElementById("graph_network_5min").style.backgroundColor = "white"
    document.getElementById("graph_network_30min").style.backgroundColor = "white"
    document.getElementById("graph_network_60min").style.backgroundColor = "white"
}
document.getElementById("graph_network_30min").onclick = function () {
    gr_net_min = 30
    document.getElementById("graph_network_30min").style.backgroundColor = "#c2c2c2"
    document.getElementById("graph_network_15min").style.backgroundColor = "white"
    document.getElementById("graph_network_5min").style.backgroundColor = "white"
    document.getElementById("graph_network_60min").style.backgroundColor = "white"
}
document.getElementById("graph_network_60min").onclick = function () {
    gr_net_min = 60
    document.getElementById("graph_network_60min").style.backgroundColor = "#c2c2c2"
    document.getElementById("graph_network_15min").style.backgroundColor = "white"
    document.getElementById("graph_network_30min").style.backgroundColor = "white"
    document.getElementById("graph_network_5min").style.backgroundColor = "white"
}

document.getElementById("all_users_5min").onclick = function () {
    all_users_canvas_time = 5
    document.getElementById("all_users_5min").style.backgroundColor = "#c2c2c2"
    document.getElementById("all_users_30min").style.backgroundColor = "white"
    document.getElementById("all_users_60min").style.backgroundColor = "white"
}
document.getElementById("all_users_30min").onclick = function () {
    all_users_canvas_time = 30
    document.getElementById("all_users_30min").style.backgroundColor = "#c2c2c2"
    document.getElementById("all_users_5min").style.backgroundColor = "white"
    document.getElementById("all_users_60min").style.backgroundColor = "white"
}
document.getElementById("all_users_60min").onclick = function () {
    all_users_canvas_time = 60
    document.getElementById("all_users_60min").style.backgroundColor = "#c2c2c2"
    document.getElementById("all_users_30min").style.backgroundColor = "white"
    document.getElementById("all_users_5min").style.backgroundColor = "white"
}

async function rmenu_async() {
    let response = await fetch("/operations_get_data?check=now_operating", {
        method: 'get',
        headers: {
            'X-Requested-Width': 'XMLHttpRequest',
            'Content-Type': 'application/json'
        }
    })

    let data = await response.json()

    gr = data
    if (gr["answer"] != "") {
        document.getElementById("operating_p").innerHTML = gr["answer"]
    } else {
        document.getElementById("operating_p").innerHTML = "NOTHING"
    }

    if (gr_net_min == 5) {
        let response = await fetch("/operations_get_data?check=network_5", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["net"]

        let maxY = Math.max.apply(Math, yValues)
        let axisNameY = ""

        if (maxY > 2048) {
            axisNameY = "Mbps"
            for (let i = 0; i < yValues.length; i++) {
                yValues[i] = yValues[i] / 1024
            }
        } else {
            axisNameY = "Kbps"
        }

        new Chart("upload_speed_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: axisNameY
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }
    if (gr_net_min == 15) {
        let response = await fetch("/operations_get_data?check=network_15", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["net"]

        let maxY = Math.max.apply(Math, yValues)
        let axisNameY = ""

        if (maxY > 2048) {
            axisNameY = "Mbps"
            for (let i = 0; i < yValues.length; i++) {
                yValues[i] = yValues[i] / 1024
            }
        } else {
            axisNameY = "Kbps"
        }



        new Chart("upload_speed_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: axisNameY
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }
    if (gr_net_min == 30) {
        let response = await fetch("/operations_get_data?check=network_30", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["net"]

        let maxY = Math.max.apply(Math, yValues)
        let axisNameY = ""

        if (maxY > 2048) {
            axisNameY = "Mbps"
            for (let i = 0; i < yValues.length; i++) {
                yValues[i] = yValues[i] / 1024
            }
        } else {
            axisNameY = "Kbps"
        }



        new Chart("upload_speed_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: axisNameY
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }
    if (gr_net_min == 60) {
        let response = await fetch("/operations_get_data?check=network_60", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["net"]

        let maxY = Math.max.apply(Math, yValues)
        let axisNameY = ""

        if (maxY > 2048) {
            axisNameY = "Mbps"
            for (let i = 0; i < yValues.length; i++) {
                yValues[i] = yValues[i] / 1024
            }
        } else {
            axisNameY = "Kbps"
        }



        new Chart("upload_speed_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: axisNameY
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }

    if (all_users_canvas_time == 5) {
        let response = await fetch("/operations_get_data?check=connected_users_all_5", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["users"]

        new Chart("all_users_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: "Users"
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }
    if (all_users_canvas_time == 30) {
        let response = await fetch("/operations_get_data?check=connected_users_all_30", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["users"]

        new Chart("all_users_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: "Users"
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }
    if (all_users_canvas_time == 60) {
        let response = await fetch("/operations_get_data?check=connected_users_all_60", {
            method: 'get',
            headers: {
                'X-Requested-Width': 'XMLHttpRequest',
                'Content-Type': 'application/json'
            }
        })

        let data = await response.json()

        gr = data

        let xValues = gr["time"]
        let yValues = gr["users"]

        new Chart("all_users_canvas", {
            type: "line",
            data: {
                labels: xValues,
                datasets: [{
                    borderColor: "rgb(255,255,255)",
                    data: yValues
                }]
            },
            options: {
                tooltips: {enabled: false},
                hover: {mode: null},
                animation: {
                    duration: 0
                },
                legend: {
                    display: false
                },
                tooltips: {
                    enabled: false
                },
                elements: {
                    point:{
                        radius: 0
                    }
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true,
                            stepSize: 1,
                            autoSkip: true,
                            maxTicksLimit: 10
                        },
                        scaleLabel: {
                            display: true,
                            labelString: "Users"
                          }
                    }],
                    xAxes: [{
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 10
                        }
                    }]
                }
            }
        })
    }
}

rmenu_async()