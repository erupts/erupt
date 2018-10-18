import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-invoice-summary',
  templateUrl: './invoice-summary.component.html',
  styleUrls: ['./invoice-summary.component.scss']
})
export class InvoiceSummaryComponent implements OnInit {
  type1 = 'bar';
  data1 = {
    labels: ['jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'],
    datasets: [{
      label: 'Sales',
      backgroundColor: [
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(95, 190, 170, 0.99)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(95, 190, 170, 0.99)'
      ],
      hoverBackgroundColor: [
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)'
      ],
      data: [65, 59, 80, 81, 56, 55, 50, 45],
    }, {
      label: 'Expense',
      backgroundColor: [
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(93, 156, 236, 0.93)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(93, 156, 236, 0.93)'
      ],
      hoverBackgroundColor: [
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(103, 162, 237, 0.82)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(26, 188, 156, 0.88)',
        'rgba(103, 162, 237, 0.82)'
      ],
      data: [60, 69, 85, 91, 58, 50, 45, 45],
    }]
  };
  options = {
    responsive: true,
    maintainAspectRatio: false,
    barValueSpacing: 20
  };

  constructor() { }

  ngOnInit() {
  }

}
