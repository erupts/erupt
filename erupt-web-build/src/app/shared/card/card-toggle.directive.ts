import {Directive, ElementRef, HostListener} from '@angular/core';

@Directive({
    selector: '[appCardToggleEvent]'
})
export class CardToggleDirective {
    constructor(private el: ElementRef) { }

    @HostListener('click', ['$event'])
    onToggle($event: any) {
        $event.preventDefault();
        this.el.nativeElement.classList.toggle('icon-minus');
        this.el.nativeElement.classList.toggle('icon-plus');
    }
}
