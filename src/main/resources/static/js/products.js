(function(){
  function toggleVariantsRow(el){
    if(!el) return;
    var id = el.dataset && el.dataset.productId ? el.dataset.productId : null;
    if(!id) return;

    var rowId = 'variants-row-' + id;
    var row = document.getElementById(rowId);
    if(!row) return;

    // Use class-based visibility to avoid unreliable computed styles
    var isOpen = row.classList.contains('open');
    if (isOpen) {
      row.classList.remove('open');
      row.style.display = 'none';
    } else {
      row.classList.add('open');
      row.style.display = 'table-row';
    }

    // Optional: toggle button text for better UX
    try {
      if (el instanceof HTMLElement) {
        var showLabel = el.getAttribute('data-toggle-label-show') || 'View variants';
        var hideLabel = el.getAttribute('data-toggle-label-hide') || 'Hide variants';
        el.textContent = isOpen ? showLabel : hideLabel;
      }
    } catch(e){ console.error(e);} 
  }

  // expose globally for hx-on or onclick usage
  window.toggleVariantsRow = toggleVariantsRow;
})();

// Handle variants row toggle and HTMX integration
(function(){
  // Handle click to toggle - close if open, otherwise let HTMX handle the request
  document.addEventListener('click', function(e){
    var btn = e.target && e.target.closest && e.target.closest('button.js-toggle-variants');
    if(!btn) return;
    
    try {
      var id = btn.getAttribute('data-product-id');
      if(!id) return;
      var row = document.getElementById('variants-row-' + id);
      if(!row) return;
      
      var isOpen = row.classList.contains('open');
      if (isOpen) {
        // If already open, close it and prevent HTMX request
        row.classList.remove('open');
        row.style.display = 'none';
        btn.textContent = 'View variants';
        e.preventDefault();
        e.stopPropagation();
        return false;
      }
      // If closed, let HTMX handle it - don't prevent
    } catch(err){ console.error(err); }
  });
  
  // Show row when HTMX request starts for variants
  document.addEventListener('htmx:beforeRequest', function(e){
    try {
      var el = e.detail && e.detail.elt;
      if(!el || !el.classList || !el.classList.contains('js-toggle-variants')) return;
      
      var id = el.getAttribute('data-product-id');
      if(!id) return;
      var row = document.getElementById('variants-row-' + id);
      if(row) {
        row.classList.add('open');
        row.style.display = 'table-row';
        el.textContent = 'Hide variants';
      }
    } catch(err){ console.error(err); }
  });
  
  // Ensure row stays visible after HTMX swaps content
  document.addEventListener('htmx:afterSwap', function(e){
    try {
      var target = e.detail && e.detail.target;
      if(!target) return;
      
      // Check if the swapped content is inside a variants-content div
      var variantsContent = target.id && target.id.indexOf('variants-content-') === 0 
        ? target 
        : (target.closest && target.closest('[id^="variants-content-"]'));
      if(variantsContent) {
        var productId = variantsContent.id.replace('variants-content-', '');
        var row = document.getElementById('variants-row-' + productId);
        if(row) {
          row.classList.add('open');
          row.style.display = 'table-row';
        }
        // Update button text
        var btn = document.querySelector('button.js-toggle-variants[data-product-id="' + productId + '"]');
        if(btn) {
          btn.textContent = 'Hide variants';
        }
      }
    } catch(err){ console.error(err); }
  });
})();

// Server-side product search (debounced), replacing previous client-side filter
(function(){
  function debounce(fn, delay){
    var t; return function(){
      var ctx = this, args = arguments;
      clearTimeout(t);
      t = setTimeout(function(){ fn.apply(ctx, args); }, delay);
    };
  }

  function currentQuery(){
    var input = document.getElementById('search');
    return input ? input.value || '' : '';
  }

  function loadTable(q){
    var target = '#products-table';
    var url = '/products/table';
    if(q && q.trim().length > 0){
      url += '?q=' + encodeURIComponent(q.trim());
    }
    if(window.htmx && typeof window.htmx.ajax === 'function'){
      window.htmx.ajax('GET', url, {target: target, swap: 'innerHTML'});
    } else {
      // Fallback: direct fetch replace
      fetch(url, { headers: { 'HX-Request': 'true' }})
        .then(function(r){ return r.text(); })
        .then(function(html){
          var el = document.querySelector(target);
          if(el) el.innerHTML = html;
        })
        .catch(function(err){ console.error(err); });
    }
  }

  function setup(){
    var input = document.getElementById('search');
    if(!input) return;
    var handler = debounce(function(){ loadTable(input.value || ''); }, 200);
    input.addEventListener('input', handler);

    // If user refreshes with a prefilled query, load filtered table
    var initial = input.value || '';
    if(initial && initial.trim().length > 0){
      loadTable(initial);
    }
  }

  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', setup);
  } else {
    setup();
  }

  // When HTMX swaps the products table (after add/delete or initial load), reapply filter
  document.addEventListener('htmx:afterSwap', function(evt){
    try {
      var target = evt && evt.detail && evt.detail.target;
      if(!target) return;
      if(target.id === 'products-table'){
        reapply();
      }
    } catch(e){ console.error(e); }
  });
})();


// Toast notifications and HTMX feedback for create product
(function(){
  function ensureContainer(){
    var c = document.getElementById('toast-container');
    if(!c){
      c = document.createElement('div');
      c.id = 'toast-container';
      c.setAttribute('aria-live','polite');
      c.setAttribute('aria-atomic','true');
      document.body.appendChild(c);
    }
    return c;
  }

  function showToast(message, type){
    var container = ensureContainer();
    var toast = document.createElement('div');
    toast.className = 'toast ' + (type === 'error' ? 'toast-error' : 'toast-success');
    var span = document.createElement('span');
    span.textContent = message || (type === 'error' ? 'Operation failed' : 'Operation succeeded');
    var btn = document.createElement('button');
    btn.type = 'button';
    btn.setAttribute('aria-label', 'Close notification');
    btn.innerHTML = '\u00D7';
    btn.addEventListener('click', function(){
      if(toast && toast.parentNode){ toast.parentNode.removeChild(toast); }
    });
    toast.appendChild(span);
    toast.appendChild(btn);
    container.appendChild(toast);
    setTimeout(function(){
      if(toast && toast.parentNode){ toast.parentNode.removeChild(toast); }
    }, 3500);
  }

  // Expose for debugging if needed
  window.showToast = showToast;

  function setupFeedbackListeners(){
    // Custom HX-Trigger based events from server
    document.addEventListener('productAdded', function(e){
      var msg = e && e.detail && e.detail.message ? e.detail.message : 'Product created successfully';
      showToast(msg, 'success');
    });
    document.addEventListener('productAddFailed', function(e){
      var msg = e && e.detail && e.detail.message ? e.detail.message : 'Failed to create product';
      showToast(msg, 'error');
    });

    // Fallback in case server returns error without HX-Trigger
    document.body.addEventListener('htmx:responseError', function(evt){
      try {
        var cfg = evt && evt.detail && evt.detail.requestConfig;
        if(cfg && (cfg.verb || '').toLowerCase() === 'post' && (cfg.path || '').indexOf('/products') === 0){
          showToast('Failed to create product', 'error');
        }
      } catch(e) { /* no-op */ }
    });
  }

  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', setupFeedbackListeners);
  } else {
    setupFeedbackListeners();
  }
})();

// Ensure the product Update form row is shown on every click (resilient across HTMX swaps)
(function(){
  document.addEventListener('click', function(e){
    try {
      var btn = e.target && e.target.closest && e.target.closest('button.btn-link[data-pid]');
      if(!btn) return;
      var pid = btn.getAttribute('data-pid');
      if(!pid) return;
      // Close other open edit rows (ensure only one is open)
      var rows = document.querySelectorAll('tr.product-edit-row');
      for(var i=0;i<rows.length;i++){
        var tr = rows[i];
        if(tr && tr.id !== ('product-edit-row-' + pid)){
          tr.style.display = 'none';
        }
      }
      // Open this product's edit row before HTMX request
      var row = document.getElementById('product-edit-row-' + pid);
      if(row){ row.style.display = 'table-row'; }
      // Let HTMX proceed normally (hx-get on the button)
    } catch(err){ console.error(err); }
  });
})();
