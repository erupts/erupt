import {Component, Input, OnDestroy, Inject, ViewEncapsulation} from '@angular/core';
import {Spinkit} from './spinkits';
import {Router, NavigationStart, NavigationEnd, NavigationCancel, NavigationError} from '@angular/router';
import {DOCUMENT} from '@angular/common';

@Component({
    selector: 'app-spinner',
    templateUrl: './spinner.component.html',
    styleUrls: [
        './spinner.component.scss',
        './spinkit-css/sk-line-material.scss'
    ],
    encapsulation: ViewEncapsulation.None
})
export class SpinnerComponent implements OnDestroy {
    public isSpinnerVisible = true;
    public Spinkit = Spinkit;
    @Input() public backgroundColor = 'rgba(255, 255, 255, 0.8)';
    @Input() public spinner = Spinkit.skLine;
    constructor(private router: Router, @Inject(DOCUMENT) private document: Document) {
        this.router.events.subscribe(event => {
            if (event instanceof NavigationStart) {
                this.isSpinnerVisible = true;
            } else if ( event instanceof NavigationEnd || event instanceof NavigationCancel || event instanceof NavigationError) {
                this.isSpinnerVisible = false;
            }
        }, () => {
            this.isSpinnerVisible = false;
        });
    }

    ngOnDestroy(): void {
        this.isSpinnerVisible = false;
    }
}
