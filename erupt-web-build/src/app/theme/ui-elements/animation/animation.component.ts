import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {AnimationBuilder, AnimationService} from 'css-animator';

@Component({
  selector: 'app-animation',
  templateUrl: './animation.component.html',
  styleUrls: [
    './animation.component.scss',
    '../../../../scss/animation.scss'
  ],
  encapsulation: ViewEncapsulation.None
})
export class AnimationComponent implements OnInit {
  public animation = 'bounce';
  public isVisible = true;
  public isAnimating = false;

  public animator: AnimationBuilder;

  constructor(animationService: AnimationService) {
    this.animator = animationService.builder();
    this.animator.useVisibility = true;
  }

  ngOnInit() {
  }

  public animate(element: HTMLElement, animation: string, status: boolean) {
    if (status) {
      this.animation = animation;
    }
    this.isAnimating = true;

    this.animator
      .setType(this.animation)
      .setDuration(1500)
      .animate(element)
      .then(() => {
        this.isAnimating = false;
      })
      .catch(e => {
        this.isAnimating = false;
        console.log('css-animator: Animation aborted', e);
      });
  }

}
