/**
 * Created by liyuepeng on 11/6/18.
 */
import {Injectable} from '@angular/core';

@Injectable()
export class ToastService {

  // constructor(private toastyService: ToastyService){
  //
  // }
  //
  // addToast(options: any) {
  //   if (options.closeOther) {
  //     this.toastyService.clearAll();
  //   }
  //   this.position = options.position ? options.position : this.position;
  //   const toastOptions: ToastOptions = {
  //     title: options.title,
  //     msg: options.msg,
  //     showClose: options.showClose,
  //     timeout: options.timeout,
  //     theme: options.theme,
  //     onAdd: (toast: ToastData) => {
  //       /* added */
  //     },
  //     onRemove: (toast: ToastData) => {
  //       /* removed */
  //     }
  //   };
  //
  //   switch (options.type) {
  //     case 'default':
  //       this.toastyService.default(toastOptions);
  //       break;
  //     case 'info':
  //       this.toastyService.info(toastOptions);
  //       break;
  //     case 'success':
  //       this.toastyService.success(toastOptions);
  //       break;
  //     case 'wait':
  //       this.toastyService.wait(toastOptions);
  //       break;
  //     case 'error':
  //       this.toastyService.error(toastOptions);
  //       break;
  //     case 'warning':
  //       this.toastyService.warning(toastOptions);
  //       break;
  //   }
  // }

}
