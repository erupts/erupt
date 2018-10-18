import {Component, Directive, ElementRef, HostListener, OnInit} from '@angular/core';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: [
    './alert.component.scss',
    '../../../../../assets/icon/icofont/css/icofont.scss'
  ]
})
export class AlertComponent implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}

@Directive({
  selector: '[appRemoveAlert]'
})
export class RemoveAlertDirective {
  alert_parent: any;
  constructor(private elements: ElementRef) {}

  @HostListener('click', ['$event'])
  onToggle($event: any) {
    $event.preventDefault();
    this.alert_parent = (this.elements).nativeElement.parentElement;
    this.alert_parent.remove();
  }
}
