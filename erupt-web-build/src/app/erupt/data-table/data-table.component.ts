import {
  AfterContentChecked, AfterContentInit, AfterViewInit, Component, ElementRef, Input,
  OnInit
} from '@angular/core';

@Component({
  selector: 'erupt-data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss']
})
export class DataTableComponent implements OnInit, AfterViewInit {

  constructor(private el: ElementRef) {

  }

  ngOnInit() {

  }


  ngAfterViewInit() {

  }


}

