import { Component, OnInit } from '@angular/core';

declare var $: any;

import '../../../../assets/charts/float/jquery.flot.js';
import '../../../../assets/charts/float/jquery.flot.categories.js';
import '../../../../assets/charts/float/curvedLines.js';
import '../../../../assets/charts/float/jquery.flot.tooltip.min.js';

@Component({
  selector: 'app-crm-dashboard',
  templateUrl: './crm-dashboard.component.html',
  styleUrls: [
    './crm-dashboard.component.scss',
    '../../../../assets/charts/radial/css/radial.scss'
  ]
})
export class CrmDashboardComponent implements OnInit {
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

  constructor() { }

  ngOnInit() {
    setTimeout(() => {
      $.plot($('#monthlyprofit-3'), [{
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
        color: '#448aff',
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

      $.plot($('#client-map-2'), [{
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
        color: '#ff5252',
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

      $.plot($('#client-map-3'), [{
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

      $.plot($('#tot-lead'), [{
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
          apply: false,
        }
      }], this.chartOption);

      $.plot($('#tot-vendor'), [{
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
          apply: false,
        }
      }], this.chartOption);

      $.plot($('#invoice-gen'), [{
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
          apply: false,
        }
      }], this.chartOption);

    }, 75);
  }

}
