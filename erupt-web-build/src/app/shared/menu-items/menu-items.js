"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
var MENUITEMS = [
    {
        label: 'Navigation',
        main: [
            {
                state: 'dashboard',
                name: 'Dashboard',
                type: 'link',
                icon: 'icon-home'
            },
            {
                state: 'widget',
                name: 'Widget',
                type: 'link',
                icon: 'icon-view-grid',
                badge: [
                    {
                        type: 'danger',
                        value: '100+'
                    }
                ]
            }
        ],
    },
    {
        label: 'UI Element',
        main: [
            {
                state: 'basic',
                name: 'Basic Components',
                type: 'sub',
                icon: 'icon-layout-grid2-alt',
                children: [
                    {
                        state: 'alert',
                        name: 'Alert'
                    },
                    {
                        state: 'breadcrumb',
                        name: 'Breadcrumbs'
                    },
                    {
                        state: 'button',
                        name: 'Button'
                    },
                    {
                        state: 'accordion',
                        name: 'Accordion'
                    },
                    {
                        state: 'generic-class',
                        name: 'Generic Class'
                    },
                    {
                        state: 'tabs',
                        name: 'Tabs'
                    },
                    {
                        state: 'label-badge',
                        name: 'Label Badge'
                    },
                    {
                        state: 'typography',
                        name: 'Typography'
                    },
                    {
                        state: 'other',
                        name: 'Other'
                    },
                ]
            },
            {
                state: 'advance',
                name: 'Advance Components',
                type: 'sub',
                icon: 'icon-crown',
                children: [
                    {
                        state: 'modal',
                        name: 'Modal'
                    },
                    {
                        state: 'notifications',
                        name: 'Notifications'
                    },
                    {
                        state: 'notify',
                        name: 'PNOTIFY',
                        badge: [
                            {
                                type: 'info',
                                value: 'New'
                            }
                        ]
                    },
                ]
            },
            {
                state: 'animations',
                name: 'Animations',
                type: 'link',
                icon: 'icon-reload rotate-refresh'
            }
        ]
    },
    {
        label: 'Forms',
        main: [
            {
                state: 'forms',
                name: 'Form Components',
                type: 'sub',
                icon: 'icon-layers',
                children: [
                    {
                        state: 'basic-elements',
                        name: 'Form Components'
                    }, {
                        state: 'add-on',
                        name: 'Form-Elements-Add-On'
                    }, {
                        state: 'advance-elements',
                        name: 'Form-Elements-Advance'
                    }, {
                        state: 'form-validation',
                        name: 'Form Validation'
                    }
                ]
            }, {
                state: 'picker',
                main_state: 'forms',
                name: 'Form Picker',
                type: 'link',
                icon: 'icon-pencil-alt',
                badge: [
                    {
                        type: 'warning',
                        value: 'New'
                    }
                ]
            }, {
                state: 'select',
                main_state: 'forms',
                name: 'Form Select',
                type: 'link',
                icon: 'icon-shortcode'
            }, {
                state: 'masking',
                main_state: 'forms',
                name: 'Form Masking',
                type: 'link',
                icon: 'icon-write'
            }
        ]
    },
    {
        label: 'Tables',
        main: [
            {
                state: 'bootstrap-table',
                name: 'Bootstrap Table',
                type: 'sub',
                icon: 'icon-receipt',
                children: [
                    {
                        state: 'basic',
                        name: 'Basic Table'
                    }, {
                        state: 'sizing',
                        name: 'Sizing Table'
                    }, {
                        state: 'border',
                        name: 'Border Table'
                    }, {
                        state: 'styling',
                        name: 'Styling Table'
                    }
                ]
            },
            {
                state: 'data-table',
                name: 'Data Table',
                type: 'sub',
                icon: 'icon-widgetized',
                children: [
                    {
                        state: 'basic',
                        name: 'Basic Table'
                    }
                ]
            }
        ]
    },
    {
        label: 'Chart And Map',
        main: [
            {
                state: 'landing',
                name: 'Landing Page',
                type: 'link',
                icon: 'icon-mobile',
                target: true
            }
        ]
    },
    {
        label: 'Pages',
        main: [
            {
                state: 'authentication',
                name: 'Authentication',
                type: 'sub',
                icon: 'icon-id-badge',
                children: [
                    {
                        state: 'login',
                        type: 'sub',
                        name: 'Login Pages',
                        children: [
                            {
                                state: 'with-bg-image',
                                name: 'With BG Image',
                                target: true
                            }, {
                                state: 'with-header-footer',
                                name: 'With Header Footer',
                                target: true
                            }, {
                                state: 'with-social',
                                name: 'With Social Icon',
                                target: true
                            }, {
                                state: 'with-social-header-footer',
                                name: 'Social With Header Footer',
                                target: true
                            }
                        ]
                    }, {
                        state: 'registration',
                        type: 'sub',
                        name: 'Registration Pages',
                        children: [
                            {
                                state: 'with-bg-image',
                                name: 'With BG Image',
                                target: true
                            }, {
                                state: 'with-header-footer',
                                name: 'With Header Footer',
                                target: true
                            }, {
                                state: 'with-social',
                                name: 'With Social Icon',
                                target: true
                            }, {
                                state: 'with-social-header-footer',
                                name: 'Social With Header Footer',
                                target: true
                            }, {
                                state: 'mulicon-step',
                                name: 'Multi Step',
                                target: true
                            }
                        ]
                    },
                    {
                        state: 'forgot',
                        name: 'Forgot Password',
                        target: true
                    },
                    {
                        state: 'lock-screen',
                        name: 'Lock Screen',
                        target: true
                    },
                ]
            }, {
                state: 'maintenance',
                name: 'Maintenance',
                type: 'sub',
                icon: 'icon-settings',
                children: [
                    {
                        state: 'error',
                        name: 'Error'
                    },
                    {
                        state: 'coming-soon',
                        name: 'Coming Soon'
                    },
                    {
                        state: 'offline-ui',
                        name: 'Offline UI'
                    }
                ]
            }, {
                state: 'user',
                name: 'User Profile',
                type: 'sub',
                icon: 'icon-user',
                children: [
                    {
                        state: 'profile',
                        name: 'User Profile'
                    }, {
                        state: 'card',
                        name: 'User Card'
                    }
                ]
            }
        ]
    },
    {
        label: 'App',
        main: [
            {
                state: 'crm-contact',
                name: 'CRM Contact',
                type: 'link',
                icon: 'icon-layout-list-thumb'
            }, {
                state: 'task',
                name: 'Task',
                type: 'sub',
                icon: 'icon-check-box',
                children: [
                    {
                        state: 'list',
                        name: 'Task List'
                    }, {
                        state: 'board',
                        name: 'Task Board'
                    }, {
                        state: 'details',
                        name: 'Task Details'
                    }, {
                        state: 'issue',
                        name: 'Issue List'
                    }
                ]
            }
        ]
    },
    {
        label: 'Extension',
        main: [
            {
                state: 'editor',
                name: 'Editor',
                type: 'sub',
                icon: 'icon-pencil-alt',
                children: [
                    {
                        state: 'none',
                        name: 'No One'
                    }
                ]
            }, {
                state: 'invoice',
                name: 'Invoice',
                type: 'sub',
                icon: 'icon-layout-media-right',
                children: [
                    {
                        state: 'basic',
                        name: 'Invoice'
                    }, {
                        state: 'summary',
                        name: 'Invoice Summary'
                    }, {
                        state: 'list',
                        name: 'Invoice List'
                    }
                ]
            }, {
                state: 'file-upload-ui',
                name: 'File Upload',
                type: 'link',
                icon: 'icon-cloud-up'
            }, {
                state: 'change-log',
                name: 'Change Log',
                type: 'link',
                icon: 'icon-list',
                badge: [
                    {
                        type: 'warning',
                        value: '1.0'
                    }
                ]
            }
        ]
    },
    {
        label: 'Other',
        main: [
            {
                state: '',
                name: 'Menu Levels',
                type: 'sub',
                icon: 'icon-direction-alt',
                children: [
                    {
                        state: '',
                        name: 'Menu Level 2.1',
                    }, {
                        state: '',
                        name: 'Menu Level 2.2',
                        type: 'sub',
                        children: [
                            {
                                state: '',
                                name: 'Menu Level 2.2.1'
                            },
                            {
                                state: '',
                                name: 'Menu Level 2.2.2'
                            }
                        ]
                    }, {
                        state: '',
                        name: 'Menu Level 2.3'
                    }, {
                        state: '',
                        name: 'Menu Level 2.4',
                        type: 'sub',
                        children: [
                            {
                                state: '',
                                name: 'Menu Level 2.4.1'
                            },
                            {
                                state: '',
                                name: 'Menu Level 2.4.2'
                            }
                        ]
                    }
                ]
            }, {
                state: 'simple-page',
                name: 'Simple Page',
                type: 'link',
                icon: 'icon-layout-sidebar-left'
            }
        ]
    }, {
        label: 'Support',
        main: [
            {
                state: 'documentation',
                name: 'Documentation',
                type: 'link',
                icon: 'icon-file'
            }, {
                state: 'submit-issue',
                name: 'Submit Issue',
                type: 'link',
                icon: 'icon-layout-list-post'
            }
        ]
    }
];
var MenuItems = (function () {
    function MenuItems() {
    }
    MenuItems.prototype.getAll = function () {
        return MENUITEMS;
    };
    return MenuItems;
}());
MenuItems = __decorate([
    core_1.Injectable()
], MenuItems);
exports.MenuItems = MenuItems;
