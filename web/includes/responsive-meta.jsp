<%-- 
    Responsive Meta Tags and CSS Includes
    Include this in all JSP pages for responsive design
--%>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, shrink-to-fit=no">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<!-- Responsive CSS First -->
<link href="${pageContext.request.contextPath}/css/responsive.css" rel="stylesheet" type="text/css"/>
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet" type="text/css"/>

<!-- Performance optimization -->
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>

<!-- External CSS - Load after local CSS -->
<link href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://use.fontawesome.com/releases/v5.11.2/css/all.css">

<!-- Responsive JavaScript -->
<script>
    // Responsive image loading
    document.addEventListener('DOMContentLoaded', function() {
        // Add responsive classes to images
        const images = document.querySelectorAll('img');
        images.forEach(img => {
            if (!img.classList.contains('img-responsive')) {
                img.classList.add('img-responsive');
            }
        });
        
        // Handle mobile menu toggle
        const menuToggle = document.querySelector('.mobile-menu-toggle');
        const mobileMenu = document.querySelector('.mobile-menu');
        
        if (menuToggle && mobileMenu) {
            menuToggle.addEventListener('click', function() {
                mobileMenu.classList.toggle('show');
            });
        }
        
        // Auto-scale font size based on viewport
        function adjustFontSize() {
            const vw = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
            const vh = Math.max(document.documentElement.clientHeight || 0, window.innerHeight || 0);
            const minDimension = Math.min(vw, vh);
            
            // Adjust base font size for very small screens
            if (vw < 360) {
                document.documentElement.style.fontSize = '14px';
            } else if (vw < 768) {
                document.documentElement.style.fontSize = '15px';
            } else {
                document.documentElement.style.fontSize = '16px';
            }
        }
        
        // Run on load and resize
        adjustFontSize();
        window.addEventListener('resize', adjustFontSize);
        
        // Handle orientation change
        window.addEventListener('orientationchange', function() {
            setTimeout(adjustFontSize, 100);
        });
    });
</script>

<style>
    /* Additional responsive utilities */
    .mobile-menu-toggle {
        display: none;
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        padding: 0.5rem;
    }
    
    .mobile-menu {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0,0,0,0.9);
        z-index: 9999;
        padding: 2rem;
    }
    
    .mobile-menu.show {
        display: block;
    }
    
    /* Show mobile menu toggle on small screens */
    @media (max-width: 768px) {
        .mobile-menu-toggle {
            display: block;
        }
        
        .desktop-menu {
            display: none;
        }
    }
    
    /* Responsive images */
    .img-responsive {
        max-width: 100%;
        height: auto;
        display: block;
    }
    
    /* Responsive videos */
    .video-responsive {
        position: relative;
        padding-bottom: 56.25%; /* 16:9 aspect ratio */
        height: 0;
        overflow: hidden;
    }
    
    .video-responsive iframe,
    .video-responsive object,
    .video-responsive embed {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
    }
    
    /* Touch-friendly buttons */
    .btn,
    button,
    input[type="submit"],
    input[type="button"] {
        min-height: 44px;
        min-width: 44px;
        touch-action: manipulation;
    }
    
    /* Improve readability on small screens */
    @media (max-width: 480px) {
        body {
            font-size: 16px; /* Prevent zoom on iOS */
        }
        
        input,
        textarea,
        select {
            font-size: 16px; /* Prevent zoom on iOS */
        }
    }
</style>
