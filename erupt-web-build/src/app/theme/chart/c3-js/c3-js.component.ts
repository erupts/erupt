import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import 'd3';
import * as c3 from 'c3';

@Component({
  selector: 'app-c3-js',
  templateUrl: './c3-js.component.html',
  styleUrls: [
    './c3-js.component.scss',
    '../../../../../node_modules/c3/c3.min.css'
  ],
  encapsulation: ViewEncapsulation.None
})
export class C3JsComponent implements OnInit {

  constructor() { }

  ngOnInit() {
    setTimeout(() => {
      const chart = c3.generate({
        bindto: '#chart',
        data: {
          columns: [
            ['data1', 300, 350, 500, -10, 0, 320],
            ['data2', 130, 100, 180, 200, 150, 50]
          ],
          types: {
            data1: 'area-spline',
            data2: 'area-spline'
            // 'line', 'spline', 'step', 'area', 'area-step' are also available to stack
          },
          colors: {
            data1: 'rgba(1, 192, 200, 0.92)',
            data2: 'rgba(26, 188, 156, 0.93)'
          },
          groups: [
            ['data1', 'data2']
          ]
        }
      });

      const chart1 = c3.generate({
        bindto: '#chart1',
        data: {
          columns: [
            ['data1', -30, 200, 200, 400, -150, 250],
            ['data2', 130, 100, -100, 200, -150, 50],
            ['data3', -230, 200, 300, -300, 250, 250]
          ],
          type: 'bar',
          groups: [
            ['data1', 'data2']
          ],

        },
        color: {
          pattern: ['#99D683', '#4C5667', '#01C0C8']
        },
        grid: {
          y: {
            lines: [{value: 0}]
          }
        }
      });

      const chart2 = c3.generate({
        bindto: '#chart2',
        data: {
          columns: [
            ['data1', 30],
            ['data2', 120],
          ],
          type: 'donut',
          onclick: function (d, i) {
            console.log('onclick', d, i);
          },
          onmouseover: function (d, i) {
            console.log('onmouseover', d, i);
          },
          onmouseout: function (d, i) {
            console.log('onmouseout', d, i);
          }
        },
        color: {
          pattern: ['#4C5667', '#1ABC9C']
        },
        donut: {
          title: 'Iris Petal Width'
        }
      });

      const chart3 = c3.generate({
        bindto: '#chart3',
        data: {
          // iris data from R
          columns: [
            ['data1', 30],
            ['data2', 120],
          ],
          type: 'pie',
          onclick: function (d, i) {
            console.log('onclick', d, i);
          },
          onmouseover: function (d, i) {
            console.log('onmouseover', d, i);
          },
          onmouseout: function (d, i) {
            console.log('onmouseout', d, i);
          }
        },
        color: {
          pattern: ['#1ABC9C', '#4C5667', '#00C292', '#AB8CE4']
        },
      });

      const chart4 = c3.generate({
        bindto: '#chart4',
        data: {
          columns: [
            ['data1', 30, 20, 50, 40, 60, 50],
            ['data2', 200, 130, 90, 240, 130, 220],
            ['data3', 300, 200, 160, 400, 250, 250],
            ['data4', 200, 130, 90, 240, 130, 220],
            ['data5', 130, 120, 150, 140, 160, 150],
            ['data6', 90, 70, 20, 50, 60, 120],
          ],
          type: 'bar',
          colors: {
            data1: '#00C292',
            data2: '#4C5667',
            data3: '#03A9F3',
            data4: '#AB8CE4',
            data5: '#a3aebd',
            data6: '#FEC107'
          },
          types: {
            data3: 'spline',
            data4: 'line',
            data6: 'area',
          },
          groups: [
            ['data1', 'data2']
          ]
        }
      });

      const chart5 = c3.generate({
        bindto: '#chart5',
        data: {
          columns: [
            ['data1', 300, 350, 300, 0, 0, 100],
            ['data2', 130, 100, 140, 200, 150, 50]
          ],
          types: {
            data1: 'step',
            data2: 'area-step'
          },
          colors: {
            data1: 'rgb(26, 188, 156)',
            data2: 'rgba(26, 188, 156, 0.61)'
          }
        }
      });

      const chart6 = c3.generate({
        bindto: '#chart6',
        size: {height: 400},
        color: {pattern: ['#2C3E50', '#1ABC9C']},
        data: {
          xs: {
            setosa: 'setosa_x',
            versicolor: 'versicolor_x',
          },
          // iris data from R
          columns: [
            ['setosa_x', 3.5, 3.0, 3.2, 3.1, 3.6, 3.9, 3.4, 3.4, 2.9, 3.1, 3.7, 3.4, 3.0, 3.0, 4.0, 4.4, 3.9, 3.5, 3.8, 3.8, 3.4, 3.7, 3.6, 3.3, 3.4, 3.0, 3.4, 3.5, 3.4, 3.2, 3.1, 3.4, 4.1, 4.2, 3.1, 3.2, 3.5, 3.6, 3.0, 3.4, 3.5, 2.3, 3.2, 3.5, 3.8, 3.0, 3.8, 3.2, 3.7, 3.3],
            ['versicolor_x', 3.2, 3.2, 3.1, 2.3, 2.8, 2.8, 3.3, 2.4, 2.9, 2.7, 2.0, 3.0, 2.2, 2.9, 2.9, 3.1, 3.0, 2.7, 2.2, 2.5, 3.2, 2.8, 2.5, 2.8, 2.9, 3.0, 2.8, 3.0, 2.9, 2.6, 2.4, 2.4, 2.7, 2.7, 3.0, 3.4, 3.1, 2.3, 3.0, 2.5, 2.6, 3.0, 2.6, 2.3, 2.7, 3.0, 2.9, 2.9, 2.5, 2.8],
            ['setosa', 0.2, 0.2, 0.2, 0.2, 0.2, 0.4, 0.3, 0.2, 0.2, 0.1, 0.2, 0.2, 0.1, 0.1, 0.2, 0.4, 0.4, 0.3, 0.3, 0.3, 0.2, 0.4, 0.2, 0.5, 0.2, 0.2, 0.4, 0.2, 0.2, 0.2, 0.2, 0.4, 0.1, 0.2, 0.2, 0.2, 0.2, 0.1, 0.2, 0.2, 0.3, 0.3, 0.2, 0.6, 0.4, 0.3, 0.2, 0.2, 0.2, 0.2],
            ['versicolor', 1.4, 1.5, 1.5, 1.3, 1.5, 1.3, 1.6, 1.0, 1.3, 1.4, 1.0, 1.5, 1.0, 1.4, 1.3, 1.4, 1.5, 1.0, 1.5, 1.1, 1.8, 1.3, 1.5, 1.2, 1.3, 1.4, 1.4, 1.7, 1.5, 1.0, 1.1, 1.0, 1.2, 1.6, 1.5, 1.6, 1.5, 1.3, 1.3, 1.3, 1.2, 1.4, 1.2, 1.0, 1.3, 1.2, 1.3, 1.3, 1.1, 1.3],
          ],
          type: 'scatter'
        },
        axis: {
          x: {
            label: 'Sepal.Width',
            tick: {
              fit: false
            }
          },
          y: {
            label: 'Petal.Width'
          }
        }
      });
    }, 1);
  }

}
