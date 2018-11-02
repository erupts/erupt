import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {AdminComponent} from "./layout/admin/admin.component";
import {AuthComponent} from "./layout/auth/auth.component";
import {BreadcrumbsComponent} from "./layout/admin/breadcrumbs/breadcrumbs.component";
import {BrowserAnimationsModule, NoopAnimationsModule} from "@angular/platform-browser/animations";
import {SharedModule} from "./shared/shared.module";
import {MenuItems} from "./shared/menu-items/menu-items";
import {MatTabsModule} from "@angular/material";

@NgModule({
  declarations: [
    AppComponent,
    AdminComponent,
    AuthComponent,
    BreadcrumbsComponent
  ],
  imports: [
    BrowserAnimationsModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    SharedModule,
    MatTabsModule
  ],
  exports: [
    BrowserAnimationsModule
  ],
  providers: [MenuItems],
  bootstrap: [AppComponent]
})
export class AppModule {
}
