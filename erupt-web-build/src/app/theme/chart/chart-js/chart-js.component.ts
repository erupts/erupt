import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-chart-js',
  templateUrl: './chart-js.component.html',
  styleUrls: ['./chart-js.component.scss']
})
export class ChartJsComponent implements OnInit {
  type = 'doughnut';
  data = {
    labels: [
      'A', 'B', 'C', 'D '
    ],
    datasets: [{
      data: [40, 10, 40, 10],
      backgroundColor: [
        '#1ABC9C',
        '#FCC9BA',
        '#B8EDF0',
        '#B4C1D7'
      ],
      borderWidth: [
        '0px',
        '0px',
        '0px',
        '0px'
      ],
      borderColor: [
        '#1ABC9C',
        '#FCC9BA',
        '#B8EDF0',
        '#B4C1D7'

      ]
    }]
  };

  /*Bar chart Start*/
  type1 = 'bar';
  data1 = {
    labels: [0, 1, 2, 3, 4, 5, 6, 7],
    datasets: [{
      label: 'My First dataset',
      backgroundColor: [
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)'
      ],
      hoverBackgroundColor: [
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)'
      ],
      data: [65, 59, 80, 81, 56, 55, 50],
    }, {
      label: 'My second dataset',
      backgroundColor: [
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)'
      ],
      hoverBackgroundColor: [
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)'
      ],
      data: [60, 69, 85, 91, 58, 50, 45],
    }]
  };

  options = {
    responsive: true,
    maintainAspectRatio: false,
  };

  /*Bar chart End*/

  /*Radar chart Start*/
  type2 = 'radar';
  data2 = {
    labels: ['Eating', 'Drinking', 'Sleeping', 'Designing', 'Coding', 'Cycling', 'Running'],
    datasets: [{
      label: 'My First dataset',
      backgroundColor: 'rgba(100, 221, 187, 0.52)',
      borderColor: 'rgba(72, 206, 168, 0.88)',
      pointBackgroundColor: 'rgba(51, 175, 140, 0.88)',
      pointBorderColor: 'rgba(44, 130, 105, 0.88)',
      pointHoverBackgroundColor: 'rgba(44, 130, 105, 0.88)',
      pointHoverBorderColor: 'rgba(107, 226, 193, 0.98)',
      data: [65, 59, 90, 81, 56, 55, 40]
    }, {
      label: 'My Second dataset',
      backgroundColor: 'rgba(255, 204, 189, 0.95)',
      borderColor: 'rgba(255, 165, 138, 0.95)',
      pointBackgroundColor: 'rgba(255, 116, 22, 0.94)',
      pointBorderColor: 'rgba(251, 142, 109, 0.95)',
      pointHoverBackgroundColor: 'rgba(251, 142, 109, 0.95)',
      pointHoverBorderColor: 'rgba(255, 165, 138, 0.95)',
      data: [28, 48, 40, 19, 96, 27, 100]
    }]
  };

  options2 = {
    scale: {
      reverse: true,
      ticks: {
        beginAtZero: true
      }
    }
  };
  /*Radar chart End*/

  /*Polar chart*/
  type3 = 'polarArea';
  data3 = {
    datasets: [{
      data: [
        11,
        16,
        7,
        3,
        14
      ],
      backgroundColor: [
        '#7E81CB',
        '#1ABC9C',
        '#B8EDF0',
        '#B4C1D7',
        '#01C0C8'
      ],
      hoverBackgroundColor: [
        '#a1a4ec',
        '#2adab7',
        '#a7e7ea',
        '#a5b0c3',
        '#10e6ef'
      ],
      label: 'My dataset' // for legend
    }],
    labels: [
      'Blue',
      'Green',
      'Light Blue',
      'grey',
      'Sea Green'
    ]
  };

  options3 = {
    elements: {
      arc: {
        borderColor: ''
      }
    }
  };
  /*Polar chart*/

  /*Pie chart*/
  type4 = 'pie';
  data4 = {
    labels: [
      'Blue',
      'Orange',
      'Sea Green'
    ],
    datasets: [{
      data: [30, 30, 40],
      backgroundColor: [
        '#25A6F7',
        '#FB9A7D',
        '#01C0C8'
      ],
      hoverBackgroundColor: [
        '#6cc4fb',
        '#ffb59f',
        '#0dedf7'
      ]
    }]
  };

  type5 = 'bubble';
  data5 = {
    datasets: [{
      label: 'First Dataset',
      data: [{
        x: 20,
        y: 20,
        r: 15
      }, {
        x: 10,
        y: 15,
        r: 10
      }, {
        x: 25,
        y: 11,
        r: 8
      }, {
        x: 15,
        y: 13,
        r: 8
      }, {
        x: 35,
        y: 18,
        r: 6
      }, {
        x: 40,
        y: 10,
        r: 10
      }],
      backgroundColor: '#FF6384',
      hoverBackgroundColor: '#FF6384',
    }]
  };
  options5 = {
    responsive: true,
    maintainAspectRatio: false,
    elements: {
      points: {
        borderWidth: 1,
        borderColor: 'rgb(0, 0, 0)'
      }
    }
  };

  type6 = 'line';
  options6 = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      xAxes: [{
        type: 'logarithmic',
        position: 'bottom',
        ticks: {
          min: 1,
          max: 1000
        }
      }]
    }
  };

  options7: {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      yAxes: [{
        time: {
          unit: 'day'
        }
      }]
    }

  };

  constructor() { }

  ngOnInit() {
  }

}
