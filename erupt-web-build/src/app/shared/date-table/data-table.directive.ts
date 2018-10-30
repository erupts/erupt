import {Directive, ElementRef, Input, OnInit} from '@angular/core';

@Directive({
  selector: '[eruptDataTable]'
})
export class DataTableDirective implements OnInit {


  @Input() config: any;

  constructor(private el: ElementRef) {

  }

  ngOnInit(): void {
    this.config.elem = this.el.nativeElement.tagName.toLowerCase();
    const config = this.config;
    layui.use('table', function () {
      layui.table.render(config);
    });
  }

}
