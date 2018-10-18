import {Component, OnDestroy, OnInit, ViewEncapsulation} from '@angular/core';

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
  selector: 'app-chart-widget',
  templateUrl: './chart-widget.component.html',
  styleUrls: [
    './chart-widget.component.scss',
    '../../../../assets/charts/radial/css/radial.scss'
  ],
  encapsulation: ViewEncapsulation.None
})
export class ChartWidgetComponent implements OnInit, OnDestroy {
  public chartOption: any = {
    legend: {
      show: false
    },
    series: {
      label: '',
      curvedLines: {
        active: true,
        nrSplinePoints: 20
      },
    },
    tooltip: {
      show: true,
      content: 'x : %x | y : %y'
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
  public monthlyInterval: any;
  public emailSentInterval: any;
  public realTime: any;
  public saleChart: any;
  public visitChart: any;

  public firstDate: any;
  public realTimeChartData = [];
  public realTimeVisitData = [];

  constructor() {
    this.firstDate = new Date();
    this.firstDate.setDate(this.firstDate.getDate() - 500);
    this.generateChartData();
    this.generateVisitChartData();
  }

  ngOnInit() {
    setTimeout(() => {

      $.plot($('#amount-processed'), [{
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
          [13, 27],
          [14, 15],
          [15, 20],
          [16, 24],
          [17, 20],
          [18, 16],
          [19, 18],
          [20, 10],
          [21, 20],
          [22, 10],
          [23, 5],
        ],
        color: '#448aff',
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($('#amount-spent'), [{
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
          [13, 27],
          [14, 15],
          [15, 20],
          [16, 24],
          [17, 20],
          [18, 16],
          [19, 18],
          [20, 10],
          [21, 20],
          [22, 10],
          [23, 5],
        ],
        color: '#9ccc65',
        bars: {
          show: true,
          lineWidth: 2,
          fill: true,
          fillColor: {
            colors: [{
              opacity: 0.7
            }, {
              opacity: 0.7
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
      $.plot($('#profit-processed'), [{
        data: [
          [0, 0],
          [1, 10],
          [2, 20],
          [3, 10],
          [4, 27],
          [5, 15],
          [6, 20],
          [7, 10],
          [8, 20],
          [9, 16],
          [10, 22],
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
        color: '#ffba57',
        lines: {
          show: true,
          fill: true,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#fff'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);

      $.plot($('#sec-ecommerce-chart-line'), [{
        data: [
          [0, 1],
          [1, 27],
          [2, 15],
          [3, 25],
          [4, 10],
          [5, 20],
          [6, 15],
          [7, 25],
          [8, 10],
          [9, 25],
          [10, 15],
          [11, 27],
          [12, 12],
          [13, 1],
        ],
        color: '#fff',
        lines: {
          show: true,
          fill: false,
          lineWidth: 2
        },
        points: {
          show: true,
          radius: 3,
          fill: true,
          fillColor: '#fff'
        },
        curvedLines: {
          apply: false,
        }
      }], this.chartOption);
      $.plot($('#sec-ecommerce-chart-bar'), [{
        data: [
          [0, 18],
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
        ],
        color: '#558B2F',
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
          barWidth: 0.6,
          align: 'center',
          horizontal: false
        },
        points: {
          show: false
        },
      }], this.chartOption);
      AmCharts.makeChart('seo-ecommerce-barchart', {
        'type': 'serial',
        'theme': 'light',
        'dataProvider': [{
          'visits': 10
        }, {
          'visits': 15
        }, {
          'visits': 12
        }, {
          'visits': 16
        }, {
          'visits': 8
        }, {
          'visits': 10
        }, {
          'visits': 9
        }, {
          'visits': 6
        }, {
          'visits': 10
        }, {
          'visits': 12
        }, {
          'visits': 10
        }, {
          'visits': 13
        }, {
          'visits': 11
        }, {
          'visits': 16
        }, {
          'visits': 8
        }, {
          'visits': 10
        }, {
          'visits': 9
        }, {
          'visits': 6
        }, {
          'visits': 10
        }, {
          'visits': 6
        }, {
          'visits': 10
        }, {
          'visits': 12
        }, {
          'visits': 10
        }, {
          'visits': 13
        }, {
          'visits': 11
        }, {
          'visits': 16
        }, {
          'visits': 8
        }, {
          'visits': 10
        }, {
          'visits': 9
        }, {
          'visits': 12
        }, {
          'visits': 10
        }, {
          'visits': 13
        }, {
          'visits': 11
        }],
        'valueAxes': [{
          'gridAlpha': 0.1,
          'dashLength': 0
        }],
        'gridAboveGraphs': true,
        'startDuration': 1,
        'graphs': [{
          'balloonText': 'Active User: <b>[[value]]</b>',
          'fillAlphas': 1,
          'lineAlpha': 1,
          'lineColor': '#448aff',
          'type': 'column',
          'valueField': 'visits',
          'columnWidth': 0.5
        }],
        'chartCursor': {
          'categoryBalloonEnabled': false,
          'cursorAlpha': 0,
          'zoomable': false
        },
        'categoryAxis': {
          'gridPosition': 'start',
          'gridAlpha': 0,
          'axesAlpha': 0,
          'lineAlpha': 0,
          'fontSize': 0,
          'tickLength': 0
        },
        'export': {
          'enabled': true
        }

      });

      $.plot($('#sal-income'), [{
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
        color: '#448aff',
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false,
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($('#rent-income'), [{
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
          [13, 1],
        ],
        color: '#9ccc65',
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false,
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($('#income-analysis'), [{
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
        color: '#ff5252',
        lines: {
          show: true,
          fill: true,
          lineWidth: 3
        },
        points: {
          show: false,
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);

      $.plot($('#sale-report-1'), [{
        data: [
          [0, 2],
          [1, 20],
          [2, 10],
          [3, 27],
          [4, 10],
          [5, 20],
          [6, 15],
          [7, 24],
          [8, 16],
          [9, 20],
          [10, 10],
          [11, 18],
          [12, 20],
          [13, 10],
          [14, 5],
        ],
        color: '#448aff',
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
      $.plot($('#sale-report-2'), [{
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
        color: '#9ccc65',
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
      $.plot($('#sale-report-3'), [{
        data: [
          [0, 2],
          [1, 20],
          [2, 10],
          [3, 27],
          [4, 10],
          [5, 20],
          [6, 15],
          [7, 24],
          [8, 16],
          [9, 20],
          [10, 10],
          [11, 18],
          [12, 20],
          [13, 10],
          [14, 5],
        ],
        color: '#ff5252',
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
      $.plot($('#sale-report-4'), [{
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
        color: '#ffba57',
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

      AmCharts.makeChart('sales-analytics', {
        'type': 'serial',
        'theme': 'light',
        'marginRight': 40,
        'marginLeft': 40,
        'autoMarginOffset': 20,
        'dataDateFormat': 'YYYY-MM-DD',
        'valueAxes': [{
          'id': 'v1',
          'axisAlpha': 0,
          'position': 'left',
          'ignoreAxisWidth': true
        }],
        'balloon': {
          'borderThickness': 1,
          'shadowAlpha': 0
        },
        'graphs': [{
          'id': 'g1',
          'balloon': {
            'drop': true,
            'adjustBorderColor': false,
            'color': '#ffffff',
            'type': 'smoothedLine'
          },
          'fillAlphas': 0.5,
          'bullet': 'round',
          'bulletBorderAlpha': 1,
          'bulletColor': '#FFFFFF',
          'lineColor': '#9ccc65',
          'bulletSize': 5,
          'hideBulletsCount': 50,
          'lineThickness': 4,
          'title': 'red line',
          'useLineColorForBulletBorder': true,
          'valueField': 'value',
          'balloonText': '<span style="font-size:18px;">[[value]]</span>'
        }],
        'chartCursor': {
          'valueLineEnabled': true,
          'valueLineBalloonEnabled': true,
          'cursorAlpha': 0,
          'zoomable': false,
          'valueZoomable': true,
          'valueLineAlpha': 0.5
        },
        'chartScrollbar': {
          'autoGridCount': true,
          'graph': 'g1',
          'oppositeAxis': true,
          'scrollbarHeight': 90
        },
        'categoryField': 'date',
        'categoryAxis': {
          'parseDates': true,
          'dashLength': 1,
          'minorGridEnabled': true
        },
        'export': {
          'enabled': true
        },
        'dataProvider': [{
          'date': '2012-07-27',
          'value': 13
        }, {
          'date': '2012-07-28',
          'value': 11
        }, {
          'date': '2012-07-29',
          'value': 15
        }, {
          'date': '2012-07-30',
          'value': 16
        }, {
          'date': '2012-07-31',
          'value': 18
        }, {
          'date': '2012-08-01',
          'value': 13
        }, {
          'date': '2012-08-02',
          'value': 22
        }, {
          'date': '2012-08-03',
          'value': 23
        }, {
          'date': '2012-08-04',
          'value': 20
        }, {
          'date': '2012-08-05',
          'value': 17
        }, {
          'date': '2012-08-06',
          'value': 16
        }, {
          'date': '2012-08-07',
          'value': 18
        }, {
          'date': '2012-08-08',
          'value': 21
        }, {
          'date': '2012-08-09',
          'value': 26
        }, {
          'date': '2012-08-10',
          'value': 24
        }, {
          'date': '2012-08-11',
          'value': 29
        }, {
          'date': '2012-08-12',
          'value': 32
        }, {
          'date': '2012-08-13',
          'value': 18
        }, {
          'date': '2012-08-14',
          'value': 24
        }, {
          'date': '2012-08-15',
          'value': 22
        }, {
          'date': '2012-08-16',
          'value': 18
        }, {
          'date': '2012-08-17',
          'value': 19
        }, {
          'date': '2012-08-18',
          'value': 14
        }, {
          'date': '2012-08-19',
          'value': 15
        }, {
          'date': '2012-08-20',
          'value': 12
        }, {
          'date': '2012-08-21',
          'value': 8
        }, {
          'date': '2012-08-22',
          'value': 9
        }, {
          'date': '2012-08-23',
          'value': 8
        }, {
          'date': '2012-08-24',
          'value': 7
        }, {
          'date': '2012-08-25',
          'value': 5
        }, {
          'date': '2012-08-26',
          'value': 11
        }, {
          'date': '2012-08-27',
          'value': 13
        }, {
          'date': '2012-08-28',
          'value': 18
        }, {
          'date': '2012-08-29',
          'value': 20
        }, {
          'date': '2012-08-30',
          'value': 29
        }, {
          'date': '2012-08-31',
          'value': 33
        }, {
          'date': '2012-09-01',
          'value': 42
        }, {
          'date': '2012-09-02',
          'value': 35
        }, {
          'date': '2012-09-03',
          'value': 31
        }, {
          'date': '2012-09-04',
          'value': 47
        }, {
          'date': '2012-09-05',
          'value': 52
        }, {
          'date': '2012-09-06',
          'value': 46
        }, {
          'date': '2012-09-07',
          'value': 41
        }, {
          'date': '2012-09-08',
          'value': 43
        }, {
          'date': '2012-09-09',
          'value': 40
        }, {
          'date': '2012-09-10',
          'value': 39
        }, {
          'date': '2012-09-11',
          'value': 34
        }, {
          'date': '2012-09-12',
          'value': 29
        }, {
          'date': '2012-09-13',
          'value': 34
        }, {
          'date': '2012-09-14',
          'value': 37
        }, {
          'date': '2012-09-15',
          'value': 42
        }, {
          'date': '2012-09-16',
          'value': 49
        }, {
          'date': '2012-09-17',
          'value': 46
        }, {
          'date': '2012-09-18',
          'value': 47
        }, {
          'date': '2012-09-19',
          'value': 55
        }, {
          'date': '2012-09-20',
          'value': 59
        }, {
          'date': '2012-09-21',
          'value': 58
        }, {
          'date': '2012-09-22',
          'value': 57
        }, {
          'date': '2012-09-23',
          'value': 61
        }, {
          'date': '2012-09-24',
          'value': 59
        }, {
          'date': '2012-09-25',
          'value': 67
        }, {
          'date': '2012-09-26',
          'value': 65
        }, {
          'date': '2012-09-27',
          'value': 61
        }, {
          'date': '2012-09-28',
          'value': 66
        }, {
          'date': '2012-09-29',
          'value': 69
        }, {
          'date': '2012-09-30',
          'value': 71
        }, {
          'date': '2012-10-01',
          'value': 67
        }, {
          'date': '2012-10-02',
          'value': 63
        }, {
          'date': '2012-10-03',
          'value': 46
        }, {
          'date': '2012-10-04',
          'value': 32
        }, {
          'date': '2012-10-05',
          'value': 21
        }, {
          'date': '2012-10-06',
          'value': 18
        }, {
          'date': '2012-10-07',
          'value': 21
        }, {
          'date': '2012-10-08',
          'value': 28
        }, {
          'date': '2012-10-09',
          'value': 27
        }, {
          'date': '2012-10-10',
          'value': 36
        }, {
          'date': '2012-10-11',
          'value': 33
        }, {
          'date': '2012-10-12',
          'value': 31
        }, {
          'date': '2012-10-13',
          'value': 30
        }, {
          'date': '2012-10-14',
          'value': 34
        }, {
          'date': '2012-10-15',
          'value': 38
        }, {
          'date': '2012-10-16',
          'value': 37
        }, {
          'date': '2012-10-17',
          'value': 44
        }, {
          'date': '2012-10-18',
          'value': 49
        }, {
          'date': '2012-10-19',
          'value': 53
        }, {
          'date': '2012-10-20',
          'value': 57
        }, {
          'date': '2012-10-21',
          'value': 60
        }, {
          'date': '2012-10-22',
          'value': 61
        }, {
          'date': '2012-10-23',
          'value': 69
        }, {
          'date': '2012-10-24',
          'value': 67
        }, {
          'date': '2012-10-25',
          'value': 72
        }, {
          'date': '2012-10-26',
          'value': 77
        }, {
          'date': '2012-10-27',
          'value': 75
        }, {
          'date': '2012-10-28',
          'value': 70
        }, {
          'date': '2012-10-29',
          'value': 72
        }, {
          'date': '2012-10-30',
          'value': 70
        }, {
          'date': '2012-10-31',
          'value': 72
        }, {
          'date': '2012-11-01',
          'value': 73
        }, {
          'date': '2012-11-02',
          'value': 67
        }, {
          'date': '2012-11-03',
          'value': 68
        }, {
          'date': '2012-11-04',
          'value': 65
        }, {
          'date': '2012-11-05',
          'value': 71
        }, {
          'date': '2012-11-06',
          'value': 75
        }, {
          'date': '2012-11-07',
          'value': 74
        }, {
          'date': '2012-11-08',
          'value': 71
        }, {
          'date': '2012-11-09',
          'value': 76
        }, {
          'date': '2012-11-10',
          'value': 77
        }, {
          'date': '2012-11-11',
          'value': 81
        }, {
          'date': '2012-11-12',
          'value': 83
        }, {
          'date': '2012-11-13',
          'value': 80
        }, {
          'date': '2012-11-14',
          'value': 81
        }, {
          'date': '2012-11-15',
          'value': 87
        }, {
          'date': '2012-11-16',
          'value': 82
        }, {
          'date': '2012-11-17',
          'value': 86
        }, {
          'date': '2012-11-18',
          'value': 80
        }, {
          'date': '2012-11-19',
          'value': 87
        }, {
          'date': '2012-11-20',
          'value': 83
        }, {
          'date': '2012-11-21',
          'value': 85
        }, {
          'date': '2012-11-22',
          'value': 84
        }, {
          'date': '2012-11-23',
          'value': 82
        }, {
          'date': '2012-11-24',
          'value': 73
        }, {
          'date': '2012-11-25',
          'value': 71
        }, {
          'date': '2012-11-26',
          'value': 75
        }, {
          'date': '2012-11-27',
          'value': 79
        }, {
          'date': '2012-11-28',
          'value': 70
        }, {
          'date': '2012-11-29',
          'value': 73
        }, {
          'date': '2012-11-30',
          'value': 61
        }, {
          'date': '2012-12-01',
          'value': 62
        }, {
          'date': '2012-12-02',
          'value': 66
        }, {
          'date': '2012-12-03',
          'value': 65
        }, {
          'date': '2012-12-04',
          'value': 73
        }, {
          'date': '2012-12-05',
          'value': 79
        }, {
          'date': '2012-12-06',
          'value': 78
        }, {
          'date': '2012-12-07',
          'value': 78
        }, {
          'date': '2012-12-08',
          'value': 78
        }, {
          'date': '2012-12-09',
          'value': 74
        }, {
          'date': '2012-12-10',
          'value': 73
        }, {
          'date': '2012-12-11',
          'value': 75
        }, {
          'date': '2012-12-12',
          'value': 70
        }, {
          'date': '2012-12-13',
          'value': 77
        }, {
          'date': '2012-12-14',
          'value': 67
        }, {
          'date': '2012-12-15',
          'value': 62
        }, {
          'date': '2012-12-16',
          'value': 64
        }, {
          'date': '2012-12-17',
          'value': 61
        }, {
          'date': '2012-12-18',
          'value': 59
        }, {
          'date': '2012-12-19',
          'value': 53
        }, {
          'date': '2012-12-20',
          'value': 54
        }, {
          'date': '2012-12-21',
          'value': 56
        }, {
          'date': '2012-12-22',
          'value': 59
        }, {
          'date': '2012-12-23',
          'value': 58
        }, {
          'date': '2012-12-24',
          'value': 55
        }, {
          'date': '2012-12-25',
          'value': 52
        }, {
          'date': '2012-12-26',
          'value': 54
        }, {
          'date': '2012-12-27',
          'value': 50
        }, {
          'date': '2012-12-28',
          'value': 50
        }, {
          'date': '2012-12-29',
          'value': 51
        }, {
          'date': '2012-12-30',
          'value': 52
        }, {
          'date': '2012-12-31',
          'value': 58
        }, {
          'date': '2013-01-01',
          'value': 60
        }, {
          'date': '2013-01-02',
          'value': 67
        }, {
          'date': '2013-01-03',
          'value': 64
        }, {
          'date': '2013-01-04',
          'value': 66
        }, {
          'date': '2013-01-05',
          'value': 60
        }, {
          'date': '2013-01-06',
          'value': 63
        }, {
          'date': '2013-01-07',
          'value': 61
        }, {
          'date': '2013-01-08',
          'value': 60
        }, {
          'date': '2013-01-09',
          'value': 65
        }, {
          'date': '2013-01-10',
          'value': 75
        }, {
          'date': '2013-01-11',
          'value': 77
        }, {
          'date': '2013-01-12',
          'value': 78
        }, {
          'date': '2013-01-13',
          'value': 70
        }, {
          'date': '2013-01-14',
          'value': 70
        }, {
          'date': '2013-01-15',
          'value': 73
        }, {
          'date': '2013-01-16',
          'value': 71
        }, {
          'date': '2013-01-17',
          'value': 74
        }, {
          'date': '2013-01-18',
          'value': 78
        }, {
          'date': '2013-01-19',
          'value': 85
        }, {
          'date': '2013-01-20',
          'value': 82
        }, {
          'date': '2013-01-21',
          'value': 83
        }, {
          'date': '2013-01-22',
          'value': 88
        }, {
          'date': '2013-01-23',
          'value': 85
        }, {
          'date': '2013-01-24',
          'value': 85
        }, {
          'date': '2013-01-25',
          'value': 80
        }, {
          'date': '2013-01-26',
          'value': 87
        }, {
          'date': '2013-01-27',
          'value': 84
        }, {
          'date': '2013-01-28',
          'value': 83
        }, {
          'date': '2013-01-29',
          'value': 84
        }, {
          'date': '2013-01-30',
          'value': 81
        }]
      });
      $.plot($('#this-month'), [{
        data: [
          [0, 18],
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
        ],
        color: '#9ccc65',
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
          barWidth: 0.6,
          align: 'center',
          horizontal: false
        },
        points: {
          show: false
        },
      }], this.chartOption);

      $.plot($('#sale-chart1'), [{
        data: [
          [0, 18],
          [20, 10],
          [35, 20],
          [50, 10],
          [65, 27],
          [75, 15],
          [90, 20],
        ],
        color: '#fff',
        lines: {
          show: true,
          fill: false,
          lineWidth: 3
        },
        points: {
          show: false
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($('#sale-chart2'), [{
        data: [
          [0, 18],
          [20, 10],
          [35, 20],
          [50, 10],
          [65, 27],
          [75, 15],
          [90, 20],
        ],
        color: '#fff',
        lines: {
          show: true,
          fill: false,
          lineWidth: 3
        },
        points: {
          show: false
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($('#sale-chart3'), [{
        data: [
          [0, 18],
          [20, 10],
          [35, 20],
          [50, 10],
          [65, 27],
          [75, 15],
          [90, 20],
        ],
        color: '#fff',
        lines: {
          show: true,
          fill: false,
          lineWidth: 3
        },
        points: {
          show: false
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);

      AmCharts.makeChart('product-sales-chart', {
        'type': 'serial',
        'theme': 'light',
        'dataDateFormat': 'YYYY-MM-DD',
        'precision': 2,
        'valueAxes': [{
          'id': 'v1',
          'title': 'Sales',
          'position': 'left',
          'autoGridCount': false,
          'labelFunction': function(value) {
            return '$' + Math.round(value) + 'M';
          }
        }, {
          'id': 'v2',
          'gridAlpha': 0.1,
          'autoGridCount': false
        }],
        'graphs': [{
          'id': 'g1',
          'valueAxis': 'v2',
          'lineThickness': 0,
          'fillAlphas': 0.9,
          'lineColor': '#448aff',
          'type': 'smoothedLine',
          'title': 'Laptop',
          'useLineColorForBulletBorder': true,
          'valueField': 'market1',
          'balloonText': '[[title]]<br /><b style="font-size: 130%">[[value]]</b>'
        }, {
          'id': 'g2',
          'valueAxis': 'v2',
          'fillAlphas': 0.9,
          'bulletColor': '#ff5252',
          'lineThickness': 0,
          'lineColor': '#ff5252',
          'type': 'smoothedLine',
          'title': 'TV',
          'useLineColorForBulletBorder': true,
          'valueField': 'market2',
          'balloonText': '[[title]]<br /><b style="font-size: 130%">[[value]]</b>'
        }, {
          'id': 'g3',
          'valueAxis': 'v2',
          'fillAlphas': 0.9,
          'bulletColor': '#9ccc65',
          'lineThickness': 0,
          'lineColor': '#9ccc65',
          'type': 'smoothedLine',
          'title': 'Mobile',
          'useLineColorForBulletBorder': true,
          'valueField': 'sales1',
          'balloonText': '[[title]]<br /><b style="font-size: 130%">[[value]]</b>'
        }],
        'chartCursor': {
          'pan': true,
          'valueLineEnabled': true,
          'valueLineBalloonEnabled': true,
          'cursorAlpha': 0,
          'valueLineAlpha': 0.2
        },
        'categoryField': 'date',
        'categoryAxis': {
          'parseDates': true,
          'gridAlpha': 0,
          'minorGridEnabled': true
        },
        'legend': {
          'position': 'top',
        },
        'balloon': {
          'borderThickness': 1,
          'shadowAlpha': 0
        },
        'export': {
          'enabled': true
        },
        'dataProvider': [{
          'date': '2013-01-01',
          'market1': 0,
          'market2': 0,
          'sales1': 0
        }, {
          'date': '2013-02-01',
          'market1': 0,
          'market2': 0,
          'sales1': 40
        }, {
          'date': '2013-03-01',
          'market1': 0,
          'market2': 0,
          'sales1': 0
        }, {
          'date': '2013-04-01',
          'market1': 30,
          'market2': 0,
          'sales1': 0
        }, {
          'date': '2013-05-01',
          'market1': 0,
          'market2': 20,
          'sales1': 0
        }, {
          'date': '2013-06-01',
          'market1': 25,
          'market2': 0,
          'sales1': 0
        }, {
          'date': '2013-07-01',
          'market1': 0,
          'market2': 0,
          'sales1': 0
        }, {
          'date': '2013-08-01',
          'market1': 0,
          'market2': 0,
          'sales1': 30
        }, {
          'date': '2013-09-01',
          'market1': 0,
          'market2': 0,
          'sales1': 0
        }, {
          'date': '2013-10-01',
          'market1': 0,
          'market2': 50,
          'sales1': 0
        }, {
          'date': '2013-11-01',
          'market1': 0,
          'market2': 0,
          'sales1': 65
        }, {
          'date': '2013-12-01',
          'market1': 0,
          'market2': 0,
          'sales1': 0
        }]
      });
      const seoChart = AmCharts.makeChart('seo-ecommerce-linechart', {
        'type': 'serial',
        'theme': 'light',
        'marginTop': 0,
        'marginRight': 0,
        'dataProvider': [{
          'year': '1950',
          'value': -0.307
        }, {
          'year': '1951',
          'value': -0.168
        }, {
          'year': '1952',
          'value': -0.073
        }, {
          'year': '1953',
          'value': -0.027
        }, {
          'year': '1954',
          'value': -0.251
        }, {
          'year': '1955',
          'value': -0.281
        }, {
          'year': '1956',
          'value': -0.348
        }, {
          'year': '1957',
          'value': -0.074
        }, {
          'year': '1958',
          'value': -0.011
        }, {
          'year': '1959',
          'value': -0.074
        }, {
          'year': '1960',
          'value': -0.124
        }, {
          'year': '1961',
          'value': -0.024
        }, {
          'year': '1962',
          'value': -0.022
        }, {
          'year': '1963',
          'value': 0
        }, {
          'year': '1964',
          'value': -0.296
        }, {
          'year': '1965',
          'value': -0.217
        }, {
          'year': '1966',
          'value': -0.147
        }, {
          'year': '1967',
          'value': -0.15
        }, {
          'year': '1968',
          'value': -0.16
        }, {
          'year': '1969',
          'value': -0.011
        }, {
          'year': '1970',
          'value': -0.068
        }, {
          'year': '1971',
          'value': -0.19
        }, {
          'year': '1972',
          'value': -0.056
        }, {
          'year': '1973',
          'value': 0.077
        }, {
          'year': '1974',
          'value': -0.213
        }, {
          'year': '1975',
          'value': -0.17
        }, {
          'year': '1976',
          'value': -0.254
        }, {
          'year': '1977',
          'value': 0.019
        }, {
          'year': '1978',
          'value': -0.063
        }, {
          'year': '1979',
          'value': 0.05
        }, {
          'year': '1980',
          'value': 0.077
        }, {
          'year': '1981',
          'value': 0.12
        }, {
          'year': '1982',
          'value': 0.011
        }, {
          'year': '1983',
          'value': 0.177
        }, {
          'year': '1984',
          'value': -0.021
        }, {
          'year': '1985',
          'value': -0.037
        }, {
          'year': '1986',
          'value': 0.03
        }, {
          'year': '1987',
          'value': 0.179
        }, {
          'year': '1988',
          'value': 0.18
        }, {
          'year': '1989',
          'value': 0.104
        }, {
          'year': '1990',
          'value': 0.255
        }, {
          'year': '1991',
          'value': 0.21
        }, {
          'year': '1992',
          'value': 0.065
        }, {
          'year': '1993',
          'value': 0.11
        }, {
          'year': '1994',
          'value': 0.172
        }, {
          'year': '1995',
          'value': 0.269
        }, {
          'year': '1996',
          'value': 0.141
        }, {
          'year': '1997',
          'value': 0.353
        }, {
          'year': '1998',
          'value': 0.548
        }, {
          'year': '1999',
          'value': 0.298
        }, {
          'year': '2000',
          'value': 0.267
        }, {
          'year': '2001',
          'value': 0.411
        }, {
          'year': '2002',
          'value': 0.462
        }, {
          'year': '2003',
          'value': 0.47
        }, {
          'year': '2004',
          'value': 0.445
        }, {
          'year': '2005',
          'value': 0.47
        }],
        'valueAxes': [{
          'axisAlpha': 0,
          // 'gridAlpha': 0,
          'dashLength': 6,
          'position': 'left'
        }],
        'graphs': [{
          'id': 'g1',
          'balloonText': '[[category]]<br><b><span style="font-size:14px;">[[value]]</span></b>',
          'bullet': 'round',
          'bulletSize': 8,
          // 'fillAlphas': 0.1,
          'lineColor': '#448aff',
          'lineThickness': 2,
          'negativeLineColor': '#ff5252',
          'type': 'smoothedLine',
          'valueField': 'value'
        }],
        'chartScrollbar': {
          'graph': 'g1',
          'gridAlpha': 0,
          'color': '#888888',
          'scrollbarHeight': 55,
          'backgroundAlpha': 0,
          'selectedBackgroundAlpha': 0.1,
          'selectedBackgroundColor': '#888888',
          'graphFillAlpha': 0,
          'autoGridCount': true,
          'selectedGraphFillAlpha': 0,
          'graphLineAlpha': 0.2,
          'graphLineColor': '#c2c2c2',
          'selectedGraphLineColor': '#888888',
          'selectedGraphLineAlpha': 1
        },
        'chartCursor': {
          'categoryBalloonDateFormat': 'YYYY',
          'cursorAlpha': 0,
          'valueLineEnabled': true,
          'valueLineBalloonEnabled': true,
          'valueLineAlpha': 0.5,
          'fullWidth': true
        },
        'dataDateFormat': 'YYYY',
        'categoryField': 'year',
        'categoryAxis': {
          'minPeriod': 'YYYY',
          'gridAlpha': 0,
          'parseDates': true,
        },
      });
      seoChart.zoomToIndexes(Math.round(seoChart.dataProvider.length * 0.3), Math.round(seoChart.dataProvider.length * 0.55));

      $.plot($('#power-card-chart1'), [{
        data: [
          [0, 18],
          [20, 10],
          [35, 20],
          [50, 10],
          [65, 27],
          [75, 15],
          [90, 20],
        ],
        color: '#ff5252',
        lines: {
          show: true,
          fill: false,
          lineWidth: 3
        },
        points: {
          show: false
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($('#power-card-chart2'), [{
        data: [
          [0, 10],
          [20, 25],
          [35, 15],
          [50, 27],
          [65, 20],
          [75, 28],
          [90, 10],
        ],
        color: '#448aff',
        lines: {
          show: true,
          fill: false,
          lineWidth: 3
        },
        points: {
          show: false
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);
      $.plot($('#power-card-chart3'), [{
        data: [
          [0, 18],
          [20, 10],
          [35, 20],
          [50, 10],
          [65, 27],
          [75, 15],
          [90, 20],
        ],
        color: '#ffba57',
        lines: {
          show: true,
          fill: false,
          lineWidth: 3
        },
        points: {
          show: false
        },
        curvedLines: {
          apply: true,
        }
      }], this.chartOption);

      AmCharts.makeChart('revenue-map', {
        'type': 'serial',
        'theme': 'light',
        'dataDateFormat': 'YYYY-MM-DD',
        'precision': 2,
        'valueAxes': [{
          'id': 'v1',
          'title': 'Sales',
          'position': 'left',
          'autoGridCount': false,
          'labelFunction': function(value) {
            return '$' + Math.round(value) + 'M';
          }
        }, {
          'id': 'v2',
          'title': 'Revenue Market',
          'gridAlpha': 0,
          'autoGridCount': false
        }],
        'graphs': [{
          'id': 'g1',
          'valueAxis': 'v2',
          'bullet': 'round',
          'bulletBorderAlpha': 1,
          'bulletColor': '#FFFFFF',
          'bulletSize': 5,
          'hideBulletsCount': 50,
          'lineThickness': 2,
          'lineColor': '#448aff',
          'type': 'smoothedLine',
          'title': 'Market Days',
          'useLineColorForBulletBorder': true,
          'valueField': 'market1',
          'balloonText': '[[title]]<br /><b style="font-size: 130%">[[value]]</b>'
        }, {
          'id': 'g2',
          'valueAxis': 'v2',
          'bullet': 'round',
          'bulletBorderAlpha': 1,
          'bulletColor': '#FFFFFF',
          'bulletSize': 5,
          'hideBulletsCount': 50,
          'lineThickness': 2,
          'lineColor': '#9ccc65',
          'type': 'smoothedLine',
          'title': 'Market Days ALL',
          'useLineColorForBulletBorder': true,
          'valueField': 'market2',
          'balloonText': '[[title]]<br /><b style="font-size: 130%">[[value]]</b>'
        }],
        'chartCursor': {
          'pan': true,
          'valueLineEnabled': true,
          'valueLineBalloonEnabled': true,
          'cursorAlpha': 0,
          'valueLineAlpha': 0.2
        },
        'categoryField': 'date',
        'categoryAxis': {
          'parseDates': true,
          'dashLength': 1,
          'minorGridEnabled': true
        },
        'legend': {
          'useGraphSettings': true,
          'position': 'top'
        },
        'balloon': {
          'borderThickness': 1,
          'shadowAlpha': 0
        },
        'export': {
          'enabled': true
        },
        'dataProvider': [{
          'date': '2013-01-16',
          'market1': 85,
          'market2': 75
        }, {
          'date': '2013-01-17',
          'market1': 74,
          'market2': 80
        }, {
          'date': '2013-01-18',
          'market1': 78,
          'market2': 88
        }, {
          'date': '2013-01-19',
          'market1': 85,
          'market2': 75
        }, {
          'date': '2013-01-20',
          'market1': 82,
          'market2': 89
        }, {
          'date': '2013-01-21',
          'market1': 83,
          'market2': 78
        }, {
          'date': '2013-01-22',
          'market1': 72,
          'market2': 92
        }, {
          'date': '2013-01-23',
          'market1': 85,
          'market2': 76
        }]
      });

      const monthly_expense = AmCharts.makeChart('monthly-expense', {
        'theme': 'light',
        'type': 'gauge',
        'axes': [{
          'topTextFontSize': 1,
          'topTextYOffset': 0,
          'topTextColor': '#fff',
          'axisColor': '#31d6ea',
          'axisThickness': 0,
          'endValue': 100,
          'gridInside': false,
          'inside': true,
          'radius': '50%',
          'fontSize': 0,
          'valueInterval': 100,
          'tickAlpha': 0,
          'startAngle': -90,
          'endAngle': 90,
          'unit': '%',
          'bandOutlineAlpha': 0,
          'bands': [{
            'color': '#d6d6d6',
            'endValue': 100,
            'innerRadius': '105%',
            'radius': '170%',
            'gradientRatio': [0],
            'startValue': 0
          }, {
            'color': '#9ccc65',
            'endValue': 0,
            'innerRadius': '105%',
            'radius': '170%',
            'gradientRatio': [0],
            'startValue': 0
          }]
        }],
        'arrows': [{
          'alpha': 1,
          'innerRadius': '0%',
          'startWidth': 13,
          'nailRadius': 10,
          'nailAlpha': 1,
          'radius': '140%'
        }]
      });
      this.monthlyInterval = setInterval(() => {
        const value = Math.round(Math.random() * 100);
        monthly_expense.arrows[0].setValue(value);
        monthly_expense.axes[0].setTopText(value + ' %');
        document.getElementById('exp-val').innerHTML = ('$ ' + (value * 10));
        monthly_expense.axes[0].bands[1].setEndValue(value);
      }, 2000);

      AmCharts.makeChart('daily-visitor', {
        'type': 'serial',
        'theme': 'light',
        'precision': 2,
        'graphs': [{
          'id': 'g1',
          'valueAxis': 'v2',
          'bullet': 'round',
          'bulletBorderAlpha': 1,
          'bulletColor': '#FFFFFF',
          'bulletSize': 5,
          'hideBulletsCount': 50,
          'lineThickness': 2,
          'lineColor': '#448aff',
          'type': 'smoothedLine',
          'title': 'Market Days',
          'useLineColorForBulletBorder': true,
          'valueField': 'market1',
          'balloonText': '[[title]]<br /><b style="font-size: 130%">[[value]]</b>'
        }],
        'chartCursor': {
          'pan': true,
          'valueLineEnabled': true,
          'valueLineBalloonEnabled': true,
          'cursorAlpha': 0,
          'valueLineAlpha': 0.2
        },
        'categoryField': 'date',
        'categoryAxis': {
          'dashLength': 1,
          'axisAlpha': 0,
          'lineAlpha': 0,
          'gridAlpha': 0,
          'minorGridEnabled': true
        },
        'legend': {
          'enabled': false
        },
        'balloon': {
          'borderThickness': 1,
          'shadowAlpha': 0
        },
        'export': {
          'enabled': true
        },
        'dataProvider': [{
          'date': '1',
          'market1': 85
        }, {
          'date': '2',
          'market1': 74
        }, {
          'date': '3',
          'market1': 78
        }, {
          'date': '4',
          'market1': 85
        }, {
          'date': '5',
          'market1': 82
        }, {
          'date': '6',
          'market1': 83
        }, {
          'date': '7',
          'market1': 72
        }, {
          'date': '8',
          'market1': 85
        }]
      });
      AmCharts.makeChart('daily-sales', {
        'type': 'serial',
        'theme': 'light',
        'dataProvider': [{
          'country': 'USA',
          'visits': 10,
          'color': '#ffba57'
        }, {
          'country': 'Russia',
          'visits': 8,
          'color': '#ff5252'
        }, {
          'country': 'South Korea',
          'visits': 5,
          'color': '#9ccc65'
        }, {
          'country': 'Canada',
          'visits': 7,
          'color': '#448aff'
        }],
        'valueAxes': [{
          'axisAlpha': 0,
          'lineAlpha': 0,
          'gridAlpha': 0,
          'position': 'left',
          'fontSize': 0
        }],
        'startDuration': 1,
        'graphs': [{
          'balloonText': '<b>[[category]]: [[value]]</b>',
          'fillColorsField': 'color',
          'fillAlphas': 0.9,
          'lineAlpha': 0.2,
          'type': 'column',
          'valueField': 'visits'
        }],
        'chartCursor': {
          'categoryBalloonEnabled': false,
          'cursorAlpha': 0,
          'zoomable': false
        },
        'categoryField': 'country',
        'categoryAxis': {
          'gridPosition': 'start',
          'axisAlpha': 1,
          'lineAlpha': 0,
          'gridAlpha': 0
        },
        'export': {
          'enabled': true
        }
      });
      AmCharts.makeChart('storage-space', {
        'type': 'pie',
        'startDuration': 0,
        'theme': 'light',
        'labelRadius': 0,
        'pullOutRadius': 0,
        'labelText': '',
        'colorField': 'color',
        'legend': {
          'enabled': false,
        },
        'innerRadius': '00%',
        'dataProvider': [{
          'country': 'Lithuania',
          'litres': 501.9,
          'color': '#448aff'
        }, {
          'country': 'Czech Republic',
          'litres': 301.9,
          'color': '#ffba57'
        }, {
          'country': 'Ireland',
          'litres': 201.1,
          'color': '#ff5252'
        }, {
          'country': 'india',
          'litres': 220.1,
          'color': '#9ccc65'
        }],
        'valueField': 'litres',
        'export': {
          'enabled': true
        }
      });

      const real_time_chart = AmCharts.makeChart('real-time-chart', {
        'type': 'serial',
        'theme': 'light',
        'hideCredits': true,
        'zoomOutButton': {
          'backgroundColor': '#000000',
          'backgroundAlpha': 0.15
        },
        'dataProvider': this.realTimeChartData,
        'categoryField': 'date',
        'categoryAxis': {
          'parseDates': true,
          'minPeriod': 'DD',
          'dashLength': 1,
          'gridAlpha': 0.15,
          'axisColor': '#DADADA'
        },
        'graphs': [{
          'id': 'g1',
          'valueField': 'visits',
          'bullet': 'round',
          'bulletBorderColor': '#FFFFFF',
          'bulletBorderThickness': 2,
          'lineThickness': 2,
          'lineColor': '#448aff',
          'negativeLineColor': '#ff5252',
          'hideBulletsCount': 50
        }],
        'chartCursor': {
          'cursorPosition': 'mouse'
        }
      });
      let days = 31;
      this.realTime = setInterval(() => {
        real_time_chart.dataProvider.shift();
        days++;
        const newDate = new Date(this.firstDate);
        newDate.setDate(newDate.getDate() + days);
        const visits = Math.round(Math.random() * 40) - 20;

        real_time_chart.dataProvider.push({
          date: newDate,
          visits: visits
        });
        real_time_chart.validateData();
      }, 1000);

      const sales_prediction_chart = AmCharts.makeChart('sales-prediction-chart', {
        'theme': 'light',
        'hideCredits': true,
        'type': 'gauge',
        'axes': [{
          'topTextFontSize': 0,
          'topTextYOffset': 0,
          'topTextColor': '#fff',
          'axisColor': '#31d6ea',
          'axisThickness': 0,
          'endValue': 100,
          'gridInside': false,
          'inside': true,
          'radius': '50%',
          'fontSize': 0.,
          'valueInterval': 100,
          'tickAlpha': 0,
          'startAngle': -140,
          'endAngle': 140,
          'unit': '%',
          'bandOutlineAlpha': 0,
          'bands': [{
            'color': '#ff9797',
            'endValue': 100,
            'innerRadius': '105%',
            'radius': '170%',
            'gradientRatio': [0],
            'startValue': 0
          }, {
            'color': '#ff5252',
            'endValue': 0,
            'innerRadius': '105%',
            'radius': '170%',
            'gradientRatio': [0],
            'startValue': 0
          }]
        }],
        'arrows': [{
          'alpha': 1,
          'innerRadius': '0%',
          'startWidth': 10,
          'nailRadius': 8,
          'nailAlpha': 1,
          'radius': '140%'
        }]
      });
      this.saleChart = setInterval(() => {
        const value = Math.round(Math.random() * 100);
        sales_prediction_chart.arrows[0].setValue(value);
        sales_prediction_chart.axes[0].setTopText(value + ' %');
        sales_prediction_chart.axes[0].bands[1].setEndValue(value);
      }, 2000);

      const emails_sent_sales_chart = AmCharts.makeChart('emails-sent-sales-chart', {
        'theme': 'light',
        'hideCredits': true,
        'type': 'gauge',
        'axes': [{
          'topTextFontSize': 0,
          'topTextYOffset': 0,
          'topTextColor': '#fff',
          'axisColor': '#31d6ea',
          'axisThickness': 0,
          'endValue': 100,
          'gridInside': false,
          'inside': true,
          'radius': '50%',
          'fontSize': 0.,
          'valueInterval': 100,
          'tickAlpha': 0,
          'startAngle': 0,
          'endAngle': 360,
          'unit': '%',
          'bandOutlineAlpha': 0,
          'bands': [{
            'color': '#448aff',
            'endValue': 100,
            'innerRadius': '105%',
            'radius': '170%',
            'gradientRatio': [0],
            'startValue': 0
          }, {
            'color': '#ff5252',
            'endValue': 0,
            'innerRadius': '105%',
            'radius': '170%',
            'gradientRatio': [0],
            'startValue': 0
          }]
        }],
        'arrows': [{
          'alpha': 0,
          'innerRadius': '100%',
          'borderAlpha': 0,
          'nailAlpha': 0,
        }]
      });
      this.emailSentInterval = setInterval(() => {
        const value = Math.round(Math.random() * 100);
        document.getElementById('opened').innerHTML = ((1000 - (value * 10)) + ' opened');
        document.getElementById('unopened').innerHTML = ((value * 10) + ' unopened');
        emails_sent_sales_chart.axes[0].bands[1].setEndValue(value);
      }, 2000);

      AmCharts.makeChart('project-earning', {
        'type': 'serial',
        'hideCredits': true,
        'theme': 'light',
        'dataProvider': [ {
          'type': 'UI',
          'visits': 10
        }, {
          'type': 'UX',
          'visits': 15
        }, {
          'type': 'Web',
          'visits': 12
        }, {
          'type': 'App',
          'visits': 16
        }, {
          'type': 'SEO',
          'visits': 8
        } ],
        'valueAxes': [ {
          'gridAlpha': 0.3,
          'gridColor': '#fff',
          'axisColor': 'transparent',
          'color': '#fff',
          'dashLength': 0
        } ],
        'gridAboveGraphs': true,
        'startDuration': 1,
        'graphs': [ {
          'balloonText': 'Active User: <b>[[value]]</b>',
          'fillAlphas': 1,
          'lineAlpha': 1,
          'lineColor': '#fff',
          'type': 'column',
          'valueField': 'visits',
          'columnWidth': 0.5
        } ],
        'chartCursor': {
          'categoryBalloonEnabled': false,
          'cursorAlpha': 0,
          'zoomable': false
        },
        'categoryField': 'type',
        'categoryAxis': {
          'gridPosition': 'start',
          'gridAlpha': 0,
          'axesAlpha': 0,
          'lineAlpha': 0,
          'fontSize' : 12,
          'color': '#fff',
          'tickLength': 0
        },
        'export': {
          'enabled': false
        }

      });

      const realtime_visit_chart = AmCharts.makeChart('realtime-visit-chart', {
        'type': 'serial',
        'theme': 'light',
        'hideCredits': true,
        'zoomOutButton': {
          'backgroundColor': '#000000',
          'backgroundAlpha': 0.15
        },
        'dataProvider': this.realTimeVisitData,
        'categoryField': 'date',
        'categoryAxis': {
          'parseDates': true,
          'minPeriod': 'DD',
          'dashLength': 1,
          'gridAlpha': 0.15,
          'axisColor': '#DADADA'
        },
        'graphs': [{
          'id': 'g1',
          'valueField': 'visits1',
          'bullet': 'round',
          'bulletBorderColor': '#FFFFFF',
          'bulletBorderThickness': 0,
          'lineThickness': 3,
          'bulletAlpha': 0,
          'type': 'smoothedLine',
          'lineColor': '#448aff',
          'hideBulletsCount': 10
        }, {
          'id': 'g2',
          'valueField': 'visits2',
          'bullet': 'round',
          'type': 'smoothedLine',
          'bulletBorderColor': '#FFFFFF',
          'bulletBorderThickness': 2,
          'lineThickness': 3,
          'bulletAlpha': 0,
          'lineColor': '#ff5252',
          'hideBulletsCount': 50
        }, {
          'id': 'g3',
          'valueField': 'visits3',
          'bullet': 'round',
          'type': 'smoothedLine',
          'bulletBorderColor': '#FFFFFF',
          'bulletBorderThickness': 0,
          'lineThickness': 3,
          'bulletAlpha': 0,
          'lineColor': '#11c15b',
          'hideBulletsCount': 0
        }],
        'chartCursor': {
          'cursorPosition': 'mouse'
        }
      });
      let day_count = 11;
      this.visitChart = setInterval(() => {
        realtime_visit_chart.dataProvider.shift();
        day_count++;
        const newDate = new Date(this.firstDate);
        newDate.setDate(newDate.getDate() + day_count);
        const visits1 = Math.round(Math.random() * 10);
        const visits2 = Math.round(Math.random() * 10);
        const visits3 = Math.round(Math.random() * 10);
        realtime_visit_chart.dataProvider.push({
          date: newDate,
          visits1: visits1,
          visits2: visits2,
          visits3: visits3
        });
        realtime_visit_chart.validateData();
      }, 2000);

    }, 1);
  }

  ngOnDestroy() {
    if (this.monthlyInterval) {
      clearInterval(this.monthlyInterval);
    }
    if (this.visitChart) {
      clearInterval(this.visitChart);
    }
    if (this.emailSentInterval) {
      clearInterval(this.emailSentInterval);
    }
    if (this.saleChart) {
      clearInterval(this.saleChart);
    }
    if (this.realTime) {
      clearInterval(this.realTime);
    }
  }

  generateChartData() {
    for (let day = 0; day < 30; day++) {
      const newDate = new Date(this.firstDate);
      newDate.setDate(newDate.getDate() + day);
      const visits = Math.round(Math.random() * 40) - 20;

      this.realTimeChartData.push({
        'date': newDate,
        'visits': visits
      });
    }
  }

  generateVisitChartData() {
    for (let day = 0; day < 10; day++) {
      const newDate = new Date(this.firstDate);
      newDate.setDate(newDate.getDate() + day);
      const visits1 = Math.round(Math.random() * 10);
      const visits2 = Math.round(Math.random() * 10);
      const visits3 = Math.round(Math.random() * 10);

      this.realTimeVisitData.push({
        'date': newDate,
        'visits1': visits1,
        'visits2': visits2,
        'visits3': visits3
      });
    }
  }

}
