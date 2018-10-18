import {Component, ElementRef, HostListener, OnInit} from '@angular/core';

declare var $: any;

import '../../../../assets/charts/float/jquery.flot.js';
import '../../../../assets/charts/float/jquery.flot.categories.js';
import '../../../../assets/charts/float/curvedLines.js';
import '../../../../assets/charts/float/jquery.flot.tooltip.min.js';

@Component({
  selector: 'app-data-widget',
  templateUrl: './data-widget.component.html',
  styleUrls: ['./data-widget.component.scss']
})
export class DataWidgetComponent implements OnInit {
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

  public projectStatus1Data: any;
  public projectStatus2Data: any;
  public projectStatus3Data: any;
  public projectStatusOption: any;

  constructor() { }

  ngOnInit() {
    setTimeout(() => {
      this.projectStatus1Data = {
        datasets: [{
          data: [10, 20],
          backgroundColor: ['#91baff', '#536dfe'],
          label: 'Dataset 1'
        }],
        labels: ['Red', 'Orange']
      };
      this.projectStatus2Data = {
        datasets: [{
          data: [10, 20],
          backgroundColor: ['#11c15b', '#9dffc6'],
          label: 'Dataset 1'
        }],
        labels: ['Red', 'Orange']
      };
      this.projectStatus3Data = {
        datasets: [{
          data: [10, 20],
          backgroundColor: ['#ff5252', '#ffa7a7'],
          label: 'Dataset 1'
        }],
        labels: ['Red', 'Orange']
      };
      this.projectStatusOption = {
        title: {
          display: !1,
        },
        tooltips: {
          display: !1,
        },
        legend: {
          display: !1,
          labels: {
            usePointStyle: !1
          }
        },
        responsive: true,

      };

      $.plot($('#app-sale1'), [{
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

      $.plot($('#app-sale2'), [{
        data: [
          [0, 10],
          [20, 25],
          [35, 27],
          [50, 10],
          [65, 20],
          [75, 10],
          [90, 18],
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

      $.plot($('#app-sale3'), [{
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

      $.plot($('#app-sale4'), [{
        data: [
          [0, 10],
          [20, 25],
          [35, 27],
          [50, 10],
          [65, 20],
          [75, 10],
          [90, 18],
        ],
        color: '#9ccc65',
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

    }, 100);
  }

}

import {Directive} from '@angular/core';

@Directive({
  selector: '[appToDoListRead]',
})
export class AppToDoListReadDirective {
  constructor(public el: ElementRef) {}

  @HostListener('click', ['$event']) onClick($event) {
    (this.el.nativeElement.parentElement).classList.toggle('done-task');
  }
}

@Directive({
  selector: '[appToDoListRemove]',
})
export class AppToDoListRemoveDirective {
  constructor(public el: ElementRef) {}

  @HostListener('click', ['$event']) onClick($event) {
    (this.el.nativeElement.parentElement.parentElement).classList.add('d-none');
  }
}
