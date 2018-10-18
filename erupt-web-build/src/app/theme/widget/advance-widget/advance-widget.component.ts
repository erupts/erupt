import { Component, OnInit } from '@angular/core';

declare const AmCharts: any;
declare var $: any;

import '../../../../assets/charts/amchart/amcharts.js';
import '../../../../assets/charts/amchart/gauge.js';
import '../../../../assets/charts/amchart/pie.js';
import '../../../../assets/charts/amchart/serial.js';
import '../../../../assets/charts/amchart/light.js';
import '../../../../assets/charts/amchart/ammap.js';
import '../../../../assets/charts/amchart/usaLow.js';

import '../../../../assets/charts/float/jquery.flot.js';
import '../../../../assets/charts/float/jquery.flot.categories.js';
import '../../../../assets/charts/float/curvedLines.js';
import '../../../../assets/charts/float/jquery.flot.tooltip.min.js';

@Component({
  selector: 'app-advance-widget',
  templateUrl: './advance-widget.component.html',
  styleUrls: ['./advance-widget.component.scss']
})
export class AdvanceWidgetComponent implements OnInit {
  public chartOption: any = {
    legend: {
      show: false
    },
    series: {
      label: "",
      curvedLines: {
        active: true,
        nrSplinePoints: 20
      },
    },
    tooltip: {
      show: true,
      content: "x : %x | y : %y"
    },
    grid: {
      hoverable: true,
      borderWidth: 0,
      labelMargin: 0,
      axisMargin: 0,
      minBorderMargin: 0,
    },
    yaxis: {
      min: 0,
      max: 30,
      color: 'transparent',
      font: {
        size: 0,
      }
    },
    xaxis: {
      color: 'transparent',
      font: {
        size: 0,
      }
    }
  };

  constructor() { }

  ngOnInit() {
    setTimeout(() => {

      AmCharts.makeChart("allocation-map", {
        "type": "map",
        "theme": "light",
        "colorSteps": 10,
        "dataProvider": {
          "map": "usaLow",
          "areas": [{
            "id": "US-AL",
            "value": 4447100
          }, {
            "id": "US-AK",
            "value": 626932
          }, {
            "id": "US-AZ",
            "value": 5130632
          }, {
            "id": "US-AR",
            "value": 2673400
          }, {
            "id": "US-CA",
            "value": 33871648
          }, {
            "id": "US-CO",
            "value": 4301261
          }, {
            "id": "US-CT",
            "value": 3405565
          }, {
            "id": "US-DE",
            "value": 783600
          }, {
            "id": "US-FL",
            "value": 15982378
          }, {
            "id": "US-GA",
            "value": 8186453
          }, {
            "id": "US-HI",
            "value": 1211537
          }, {
            "id": "US-ID",
            "value": 1293953
          }, {
            "id": "US-IL",
            "value": 12419293
          }, {
            "id": "US-IN",
            "value": 6080485
          }, {
            "id": "US-IA",
            "value": 2926324
          }, {
            "id": "US-KS",
            "value": 2688418
          }, {
            "id": "US-KY",
            "value": 4041769
          }, {
            "id": "US-LA",
            "value": 4468976
          }, {
            "id": "US-ME",
            "value": 1274923
          }, {
            "id": "US-MD",
            "value": 5296486
          }, {
            "id": "US-MA",
            "value": 6349097
          }, {
            "id": "US-MI",
            "value": 9938444
          }, {
            "id": "US-MN",
            "value": 4919479
          }, {
            "id": "US-MS",
            "value": 2844658
          }, {
            "id": "US-MO",
            "value": 5595211
          }, {
            "id": "US-MT",
            "value": 902195
          }, {
            "id": "US-NE",
            "value": 1711263
          }, {
            "id": "US-NV",
            "value": 1998257
          }, {
            "id": "US-NH",
            "value": 1235786
          }, {
            "id": "US-NJ",
            "value": 8414350
          }, {
            "id": "US-NM",
            "value": 1819046
          }, {
            "id": "US-NY",
            "value": 18976457
          }, {
            "id": "US-NC",
            "value": 8049313
          }, {
            "id": "US-ND",
            "value": 642200
          }, {
            "id": "US-OH",
            "value": 11353140
          }, {
            "id": "US-OK",
            "value": 3450654
          }, {
            "id": "US-OR",
            "value": 3421399
          }, {
            "id": "US-PA",
            "value": 12281054
          }, {
            "id": "US-RI",
            "value": 1048319
          }, {
            "id": "US-SC",
            "value": 4012012
          }, {
            "id": "US-SD",
            "value": 754844
          }, {
            "id": "US-TN",
            "value": 5689283
          }, {
            "id": "US-TX",
            "value": 20851820
          }, {
            "id": "US-UT",
            "value": 2233169
          }, {
            "id": "US-VT",
            "value": 608827
          }, {
            "id": "US-VA",
            "value": 7078515
          }, {
            "id": "US-WA",
            "value": 5894121
          }, {
            "id": "US-WV",
            "value": 1808344
          }, {
            "id": "US-WI",
            "value": 5363675
          }, {
            "id": "US-WY",
            "value": 493782
          }]
        },

        "areasSettings": {
          "autoZoom": true
        },
        "export": {
          "enabled": true
        }

      });
      AmCharts.makeChart("allocation-chart", {
        "type": "pie",
        "startDuration": 0,
        "theme": "light",
        "labelRadius": 0,
        "pullOutRadius": 0,
        "labelText": "",
        "colorField": "color",
        "legend": {
          // "enabled":false,
        },
        "innerRadius": "70%",
        "dataProvider": [{
          "country": "Lithuania",
          "litres": 501.9,
          "color": "#85C5E3"
        }, {
          "country": "Czech Republic",
          "litres": 301.9,
          "color": "#6AA3C4"
        }, {
          "country": "Ireland",
          "litres": 201.1,
          "color": "#6097B9"
        }, {
          "country": "india",
          "litres": 220.1,
          "color": "#4E81A4"
        }],
        "valueField": "litres",
      });

      $.plot($("#seo-chart1"), [{
        data: [
          [0, 0],
          [1, 10],
          [2, 20],
          [3, 10],
          [4, 27],
          [5, 15],
          [6, 20],
          [7, 24],
          [8, 20],
          [9, 16],
          [10, 18],
          [11, 10],
          [12, 20],
          [13, 10],
          [14, 27],
          [15, 20],
          [16, 10],
          [17, 15],
          [18, 12],
          [19, 27],
          [20, 20],
          [21, 15],
          [22, 0],
        ],
        color: "#448aff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#448aff'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#seo-chart2"), [{
        data: [
          [0, 2],
          [1, 10],
          [2, 20],
          [3, 10],
          [4, 27],
          [5, 15],
          [6, 20],
          [7, 24],
          [8, 20],
          [9, 16],
          [10, 18],
          [11, 10],
          [12, 20],
          [13, 10],
          [14, 5],
        ],
        color: "#9ccc65",
        bars: {
          show: true,
          lineWidth: 1,
          fill: true,
          fillColor: {
            colors: [{
              opacity: 1
            }, {
              opacity: 1
            }]
          },
          barWidth: 0.5,
          align: 'center',
          horizontal: false
        },
        points: {
          show: false
        },
      }], this.chartOption);
      $.plot($("#seo-chart3"), [{
        data: [
          [0, 0],
          [1, 20],
          [2, 10],
          [3, 20],
          [4, 15],
          [5, 27],
          [6, 24],
          [7, 20],
          [8, 16],
          [9, 20],
          [10, 10],
          [11, 18],
          [12, 10],
          [13, 20],
          [14, 20],
          [15, 27],
          [16, 15],
          [17, 10],
          [18, 27],
          [19, 12],
          [20, 15],
          [21, 20],
          [22, 0],
        ],
        color: "#ff5252",
        lines: {
          show: true,
          fill: false,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#ff5252'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);

      $.plot($("#seo-anlytics1"), [{
        data: [
          [0, 10],
          [1, 25],
          [2, 15],
          [3, 26],
          [4, 15],
          [5, 15],
          [6, 20],
          [7, 25],
          [8, 20],
          [9, 25],
          [10, 10],
          [11, 12],
          [12, 27],
          [13, 1],
        ],
        color: "#448aff",
        lines: {
          show: true,
          fill: false,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#448aff'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#seo-anlytics2"), [{
        data: [
          [0, 10],
          [1, 15],
          [2, 25],
          [3, 15],
          [4, 26],
          [5, 20],
          [6, 15],
          [7, 20],
          [8, 25],
          [9, 10],
          [10, 25],
          [11, 27],
          [12, 12],
          [13, 1],
        ],
        color: "#9ccc65",
        lines: {
          show: true,
          fill: false,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#9ccc65'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#seo-anlytics3"), [{
        data: [
          [0, 10],
          [1, 25],
          [2, 15],
          [3, 26],
          [4, 15],
          [5, 15],
          [6, 20],
          [7, 25],
          [8, 20],
          [9, 25],
          [10, 10],
          [11, 12],
          [12, 27],
          [13, 1],
        ],
        color: "#ff5252",
        lines: {
          show: true,
          fill: false,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#ff5252'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#seo-anlytics4"), [{
        data: [
          [0, 10],
          [1, 15],
          [2, 25],
          [3, 15],
          [4, 26],
          [5, 20],
          [6, 15],
          [7, 20],
          [8, 25],
          [9, 10],
          [10, 25],
          [11, 27],
          [12, 12],
          [13, 1],
        ],
        color: "#ffba57",
        lines: {
          show: true,
          fill: false,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#ffba57'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);

      $.plot($("#tot-lead"), [{
        data: [
          [0, 25],
          [1, 15],
          [2, 20],
          [3, 27],
          [4, 10],
          [5, 20],
          [6, 10],
          [7, 26],
          [8, 20],
          [9, 10],
          [10, 25],
          [11, 27],
          [12, 12],
          [13, 26],
        ],
        color: "#448aff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false,
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#tot-vendor"), [{
        data: [
          [0, 25],
          [1, 15],
          [2, 25],
          [3, 27],
          [4, 10],
          [5, 20],
          [6, 15],
          [7, 26],
          [8, 20],
          [9, 13],
          [10, 25],
          [11, 27],
          [12, 12],
          [13, 20],
        ],
        color: "#9ccc65",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false,
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#invoice-gen"), [{
        data: [
          [0, 25],
          [1, 30],
          [2, 25],
          [3, 27],
          [4, 10],
          [5, 20],
          [6, 15],
          [7, 26],
          [8, 10],
          [9, 13],
          [10, 25],
          [11, 27],
          [12, 12],
          [13, 27],
        ],
        color: "#ff5252",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false,
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);

      $.plot($("#total-value-graph-1"), [{
        data: [
          [0, 20],
          [20, 10],
          [35, 18],
          [50, 12],
          [65, 25],
          [75, 10],
          [90, 20],
        ],
        color: "#fff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false
        },
        //curve the line  (old pre 1.0.0 plotting function)
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($("#total-value-graph-2"), [{
        data: [
          [0, 10],
          [20, 20],
          [35, 18],
          [50, 25],
          [65, 12],
          [75, 10],
          [90, 20],
        ],
        color: "#fff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false
        },
        //curve the line  (old pre 1.0.0 plotting function)
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($("#total-value-graph-3"), [{
        data: [
          [0, 20],
          [20, 10],
          [35, 25],
          [50, 18],
          [65, 18],
          [75, 10],
          [90, 12],
        ],
        color: "#fff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false
        },
        //curve the line  (old pre 1.0.0 plotting function)
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($("#total-value-graph-4"), [{
        data: [
          [0, 18],
          [20, 10],
          [35, 20],
          [50, 10],
          [65, 12],
          [75, 25],
          [90, 20],
        ],
        color: "#fff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false
        },
        //curve the line  (old pre 1.0.0 plotting function)
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);

      $.plot($("#client-map-1"), [{
        data: [
          [0, 20],
          [20, 10],
          [35, 18],
          [50, 12],
          [65, 25],
          [75, 10],
          [90, 20],
        ],
        color: "#448aff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false
        },
        //curve the line  (old pre 1.0.0 plotting function)
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($("#client-map-2"), [{
        data: [
          [0, 0],
          [1, 10],
          [2, 20],
          [3, 10],
          [4, 27],
          [5, 15],
          [6, 20],
          [7, 24],
          [8, 20],
          [9, 16],
          [10, 18],
          [11, 10],
          [12, 20],
          [13, 10],
          [14, 27],
          [15, 20],
          [16, 10],
          [17, 15],
          [18, 12],
          [19, 27],
          [20, 20],
          [21, 15],
          [22, 0],
        ],
        color: "#ff5252",
        lines: {
          show: true,
          fill: true,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#ff5252'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#client-map-3"), [{
        data: [
          [0, 2],
          [1, 10],
          [2, 20],
          [3, 10],
          [4, 27],
          [5, 15],
          [6, 20],
          [7, 24],
          [8, 20],
          [9, 16],
          [10, 18],
          [11, 10],
          [12, 20],
          [13, 10],
          [14, 5],
        ],
        color: "#9ccc65",
        bars: {
          show: true,
          lineWidth: 1,
          fill: true,
          fillColor: {
            colors: [{
              opacity: 1
            }, {
              opacity: 1
            }]
          },
          barWidth: 0.5,
          align: 'center',
          horizontal: false
        },
        points: {
          show: false
        },
      }], this.chartOption);

      $.plot($("#monthlyprofit-1"), [{
        data: [
          [0, 10],
          [1, 25],
          [2, 15],
          [3, 26],
          [4, 15],
          [5, 15],
          [6, 20],
          [7, 25],
          [8, 20],
          [9, 25],
          [10, 10],
          [11, 12],
          [12, 27],
          [13, 20],
          [14, 25],
          [15, 20],
          [16, 25],
          [17, 10],
          [18, 12],
          [19, 27],
          [20, 1],
        ],
        color: "#448aff",
        lines: {
          show: true,
          fill: true,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 2,
          fill: true,
          fillColor: '#448aff'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#monthlyprofit-2"), [{
        data: [
          [0, 10],
          [1, 15],
          [2, 25],
          [3, 15],
          [4, 26],
          [5, 20],
          [6, 15],
          [7, 20],
          [8, 25],
          [9, 10],
          [10, 25],
          [11, 27],
          [12, 12],
          [13, 1],
        ],
        color: "#9ccc65",
        lines: {
          show: true,
          fill: true,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 2,
          fill: true,
          fillColor: '#9ccc65'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($("#monthlyprofit-3"), [{
        data: [
          [0, 10],
          [1, 25],
          [2, 15],
          [3, 26],
          [4, 15],
          [5, 15],
          [6, 20],
          [7, 25],
          [8, 20],
          [9, 25],
          [10, 10],
          [11, 12],
          [12, 20],
          [13, 25],
          [14, 10],
          [15, 12],
          [16, 27],
          [17, 1],
        ],
        color: "#ff5252",
        lines: {
          show: true,
          fill: true,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 2,
          fill: true,
          fillColor: '#ff5252'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);

    }, 75);
  }

}
