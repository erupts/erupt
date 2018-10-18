import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-google-chart',
  templateUrl: './google-chart.component.html',
  styleUrls: ['./google-chart.component.scss']
})
export class GoogleChartComponent implements OnInit {
  areaChartData =  {
    chartType: 'AreaChart',
    dataTable: [
      ['Year', 'Sales', 'Expenses'],
      ['2013', 1000, 400],
      ['2014', 1170, 460],
      ['2015', 660, 1120],
      ['2016', 1030, 540]
    ],
    options: {
      vAxis: { minValue: 0 },
      colors: ['#01C0C8', '#FB9678'],
      height: 300
    },
  };

  stackingAreaChartData =  {
    chartType: 'AreaChart',
    dataTable: [
      ['Year', 'Sales', 'Expenses'],
      ['2013', 1000, 400],
      ['2014', 1170, 460],
      ['2015', 660, 1120],
      ['2016', 1030, 540]
    ],
    options: {
      isStacked: true,
      height: 300,
      legend: { position: 'top', maxLines: 3 },
      vAxis: { minValue: 0 },
      colors: ['#2ecc71', '#5faee3']
    },
  };

  barChartData =  {
    chartType: 'BarChart',
    dataTable: [
      ['City', '2010 Population', '2000 Population'],
      ['New York City, NY', 8175000, 8008000],
      ['Los Angeles, CA', 3792000, 3694000],
      ['Chicago, IL', 2695000, 2896000],
      ['Houston, TX', 2099000, 1953000],
      ['Philadelphia, PA', 1526000, 1517000]
    ],
    options: {
      height: 300,
      title: 'Population of Largest U.S. Cities',
      chartArea: { width: '50%' },
      isStacked: true,
      hAxis: {
        title: 'Total Population',
        minValue: 0,
      },
      vAxis: {
        title: 'City'
      },
      colors: ['#2ecc71', '#5faee3']
    },
  };

  bubbleChartData =  {
    chartType: 'BubbleChart',
    dataTable: [
      ['ID', 'Life Expectancy', 'Fertility Rate', 'Region', 'Population'],
      ['CAN', 80.66, 1.67, 'North America', 33739900],
      ['DEU', 79.84, 1.36, 'Europe', 81902307],
      ['DNK', 78.6, 1.84, 'Europe', 5523095],
      ['EGY', 72.73, 2.78, 'Middle East', 79716203],
      ['GBR', 80.05, 2, 'Europe', 61801570],
      ['IRN', 72.49, 1.7, 'Middle East', 73137148],
      ['IRQ', 68.09, 4.77, 'Middle East', 31090763],
      ['ISR', 81.55, 2.96, 'Middle East', 7485600],
      ['RUS', 68.6, 1.54, 'Europe', 141850000],
      ['USA', 78.09, 2.05, 'North America', 307007000]
    ],
    options: {
      height: 300,
      title: 'Correlation between life expectancy, fertility rate ' +
      'and population of some world countries (2010)',
      hAxis: { title: 'Life Expectancy' },
      vAxis: { title: 'Fertility Rate' },
      bubble: { textStyle: { fontSize: 11 } },
      colors: ['#2ecc71', '#5faee3', '#DD4B39']
    },
  };

  comboChartData =  {
    chartType: 'ComboChart',
    dataTable: [
      ['Month', 'Bolivia', 'Ecuador', 'Madagascar', 'Papua New Guinea', 'Rwanda', 'Average'],
      ['2004/05', 165, 938, 522, 998, 450, 614.6],
      ['2005/06', 135, 1120, 599, 1268, 288, 682],
      ['2006/07', 157, 1167, 587, 807, 397, 623],
      ['2007/08', 139, 1110, 615, 968, 215, 609.4],
      ['2008/09', 136, 691, 629, 1026, 366, 569.6]
    ],
    options: {
      height: 300,
      title: 'Monthly Coffee Production by Country',
      vAxis: { title: 'Cups' },
      hAxis: { title: 'Month' },
      seriesType: 'bars',
      series: { 5: { type: 'line' } },
      colors: ['#2ecc71', '#01C0C8', '#FB9678', '#5faee3', '#f4d03f', '#e74c3c']
    },
  };

  donutChartData =  {
    chartType: 'PieChart',
    dataTable: [
      ['Task', 'Hours per Day'],
      ['Work', 11],
      ['Eat', 2],
      ['Commute', 2],
      ['Watch TV', 2],
      ['Sleep', 7]
    ],
    options: {
      height: 300,
      title: 'My Daily Activities',
      pieHole: 0.4,
      colors: ['#2ecc71', '#01C0C8', '#FB9678', '#5faee3', '#f4d03f']
    },
  };

  exploadingChartData =  {
    chartType: 'PieChart',
    dataTable: [
      ['Language', 'Speakers (in millions)'],
      ['Assamese', 13],
      ['Bengali', 83],
      ['Bodo', 1.4],
      ['Dogri', 2.3],
      ['Gujarati', 46],
      ['Hindi', 300],
      ['Kannada', 38],
      ['Kashmiri', 5.5],
      ['Konkani', 5],
      ['Maithili', 20],
      ['Malayalam', 33],
      ['Manipuri', 1.5],
      ['Marathi', 72],
      ['Nepali', 2.9],
      ['Oriya', 33]
    ],
    options: {
      height: 300,
      title: 'Indian Language Use',
      legend: 'none',
      pieSliceText: 'label',
      slices: {
        4: { offset: 0.2 },
        12: { offset: 0.3 },
        14: { offset: 0.4 },
        15: { offset: 0.5 },
      },
      colors: ['#2ecc71', '#01C0C8', '#FB9678', '#5faee3', '#f4d03f', '#e74c3c']
    },
  };

  tableChartData =  {
    chartType: 'Table',
    dataTable: [
      ['Department', 'Revenues', 'Another column'],
      ['Shoes', 10700, -100],
      ['Sports', -15400, 25],
      ['Toys', 12500, 40],
      ['Electronics', -2100, 889],
      ['Food', 22600, 78],
      ['Art', 1100, 42]
    ],
    formatters: [
      {
        columns: [1, 2],
        type: 'NumberFormat',
        options: {
          prefix: '&euro;', negativeColor: 'red', negativeParens: true
        }
      }
    ],
    options: {
      height: 300,
      title: 'Countries',
      allowHtml: true
    }
  };

  constructor() { }

  ngOnInit() {
  }

}
