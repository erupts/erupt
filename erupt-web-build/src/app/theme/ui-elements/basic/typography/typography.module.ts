import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TypographyComponent } from './typography.component';
import {TypographyRoutingModule} from './typography-routing.module';
import {SharedModule} from '../../../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    TypographyRoutingModule,
    SharedModule
  ],
  declarations: [TypographyComponent]
})
export class TypographyModule { }
