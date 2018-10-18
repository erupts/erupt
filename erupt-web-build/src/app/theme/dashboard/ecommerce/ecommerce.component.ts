import { Component, OnInit } from '@angular/core';

declare var $: any;

import '../../../../assets/charts/float/jquery.flot.js';
import '../../../../assets/charts/float/jquery.flot.categories.js';
import '../../../../assets/charts/float/curvedLines.js';
import '../../../../assets/charts/float/jquery.flot.tooltip.min.js';

@Component({
  selector: 'app-ecommerce',
  templateUrl: './ecommerce.component.html',
  styleUrls: [
    './ecommerce.component.scss',
    '../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class EcommerceComponent implements OnInit {
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
      $.plot($('#seo-anlytics1'), [{
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
        color: '#448aff',
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

      $.plot($('#seo-anlytics2'), [{
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
        color: '#9ccc65',
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

      $.plot($('#seo-anlytics3'), [{
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
        color: '#ff5252',
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

      $.plot($('#seo-anlytics4'), [{
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
        color: '#ffba57',
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

      $.plot($('#monthlyprofit-1'), [{
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

      $.plot($('#monthlyprofit-2'), [{
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
        color: '#9ccc65',
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

    }, 75);
  }

}

