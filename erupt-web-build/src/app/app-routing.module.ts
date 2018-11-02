import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AdminComponent} from "./layout/admin/admin.component";

const routes: Routes = [
  {
    path: '',
    component: AdminComponent,
    children: [
      {
        path: '',
        redirectTo: 'list/table',
        pathMatch: 'full'
      },

      {
        path: 'list',
        loadChildren: './build/build.module#BuildModule'
      }
    ]
  },
  // {
  //   path: '',
  //   component: AuthComponent,
  //   children: [
  //     {
  //       path: 'auth',
  //       loadChildren: './theme/auth/auth.module#AuthModule'
  //     },
  //     {
  //       path: 'maintenance/offline-ui',
  //       loadChildren: './theme/maintenance/offline-ui/offline-ui.module#OfflineUiModule'
  //     }
  //   ]
  // }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
