import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnimationComponent } from './animation.component';
import {AnimationRoutingModule} from './animation-routing.module';
import {SharedModule} from '../../../shared/shared.module';
import {AnimationService, AnimatorModule} from 'css-animator';

@NgModule({
  imports: [
    CommonModule,
    AnimationRoutingModule,
    SharedModule,
    AnimatorModule
  ],
  declarations: [AnimationComponent],
  providers: [AnimationService]
})
export class AnimationModule { }
