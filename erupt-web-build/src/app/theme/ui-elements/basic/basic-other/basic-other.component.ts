import {Component, OnInit, ViewEncapsulation} from '@angular/core';

@Component({
  selector: 'app-basic-other',
  templateUrl: './basic-other.component.html',
  styleUrls: ['./basic-other.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class BasicOtherComponent implements OnInit {
  public maxSize = 5;
  public bigTotalItems = 175;

  public totalItems = 64;
  public pageAdvance = 1;

  constructor() { }

  ngOnInit() {
  }

}
